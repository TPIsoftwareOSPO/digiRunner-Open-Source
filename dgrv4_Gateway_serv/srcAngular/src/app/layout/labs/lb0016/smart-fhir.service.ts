import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';
import {
  Bundle, FhirResource, Patient, Practitioner, Encounter,
  HumanName, CodeableConcept,
  SmartConfiguration, SmartTokenResponse, SmartLaunchResponse,
  PatientDisplay, PractitionerDisplay, EncounterDisplay,
} from './smart-fhir.types';

// 對外 re-export，維持既有 import 來源（元件仍從本 service 取用顯示型別）
export {
  PatientDisplay, PractitionerDisplay, EncounterDisplay,
  Bundle, Patient, Practitioner, Encounter,
  SmartConfiguration, SmartTokenResponse, SmartLaunchResponse,
} from './smart-fhir.types';

const FORM_URLENCODED = 'application/x-www-form-urlencoded';

/** 將 Record 轉為 application/x-www-form-urlencoded 字串 */
function toFormBody(form: Record<string, string>): string {
  let params = new HttpParams();
  Object.keys(form).forEach(k => { params = params.set(k, form[k]); });
  return params.toString();
}

@Injectable()
export class SmartFhirService {

  constructor(private http: HttpClient, private toolService: ToolService) {}

  private get launcherHeaders(): HttpHeaders {
    const token = this.toolService.getToken();
    let headers = new HttpHeaders({ 'X-Smart-Launcher': 'true' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  private buildFhirUrl(proxyName: string, fhirBasePath: string, resource: string): string {
    const path = !fhirBasePath || fhirBasePath === '/' ? '' : fhirBasePath;
    return `/smart-on-fhir/${proxyName}${path}/${resource}`;
  }

  searchPatients(proxyName: string, fhirBasePath: string, keyword: string, count: number = 50, offset: number = 0, searchBy: 'name' | 'id' = 'name'): Observable<Bundle<Patient>> {
    const url = this.buildFhirUrl(proxyName, fhirBasePath, 'Patient');
    const params: Record<string, string> = {
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    if (keyword) {
      params[searchBy === 'id' ? '_id' : 'name:contains'] = keyword;
    }
    return this.http.get<Bundle<Patient>>(url, { params, headers: this.launcherHeaders });
  }

  searchEncounters(proxyName: string, fhirBasePath: string, patientId: string, count: number = 20, offset: number = 0): Observable<Bundle<Encounter>> {
    const url = this.buildFhirUrl(proxyName, fhirBasePath, 'Encounter');
    const params: Record<string, string> = {
      'subject': `Patient/${patientId}`,
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    return this.http.get<Bundle<Encounter>>(url, { params, headers: this.launcherHeaders });
  }

  searchPractitioners(proxyName: string, fhirBasePath: string, keyword: string, count: number = 50, offset: number = 0, searchBy: 'name' | 'id' = 'name'): Observable<Bundle<Practitioner>> {
    const url = this.buildFhirUrl(proxyName, fhirBasePath, 'Practitioner');
    const params: Record<string, string> = {
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    if (keyword) {
      params[searchBy === 'id' ? '_id' : 'name:contains'] = keyword;
    }
    return this.http.get<Bundle<Practitioner>>(url, { params, headers: this.launcherHeaders });
  }

  /**
   * 從完整的 Diversion URL 解析出 fhirBasePath（path 部分）。
   * 例：
   *   "https://hapi.fhir.tw/fhir"  → "/fhir"
   *   "https://r4.smarthealthit.org/" → "/"
   *   "https://example.com" → "/"
   */
  static extractFhirBasePath(diversionUrl: string | undefined): string {
    if (!diversionUrl) return '/';
    try {
      const u = new URL(diversionUrl);
      const path = u.pathname;
      if (!path || path === '/') return '/';
      return path.endsWith('/') ? path.slice(0, -1) : path;
    } catch {
      return '/';
    }
  }

  fetchByUrl(url: string): Observable<FhirResource> {
    return this.http.get<FhirResource>(url, { headers: this.launcherHeaders });
  }

  /** 用 FHIR Bundle.link 的 URL（next/previous）直接抓下一頁 */
  fetchByLink(url: string): Observable<Bundle> {
    return this.http.get<Bundle>(this.toRelative(url), { headers: this.launcherHeaders });
  }

  // ==================== SMART Launch / OAuth ====================
  //
  // 注意：除 createLaunch（launcher 後台觸發 launch context）外，
  // 以下方法模擬「外部 SMART App」的 OAuth flow，刻意「不帶」admin launcher Bearer——
  // 它們代表第三方 App 以自身（public / 取得的）token 進行，加上 admin token 反而錯誤。

  /** 觸發 EHR / patient portal launch context，回傳 launch token。 */
  createLaunch(form: Record<string, string>, basicAuth?: string): Observable<SmartLaunchResponse> {
    let headers = new HttpHeaders({ 'Content-Type': FORM_URLENCODED });
    if (basicAuth) {
      headers = headers.set('Authorization', 'Basic ' + basicAuth);
    }
    return this.http.post<SmartLaunchResponse>('/dgrv4/ssotoken/smart/launch', toFormBody(form), { headers });
  }

  /** 探索 SMART configuration（外部 App 視角，無 auth）。 */
  discoverSmartConfig(issUrl: string): Observable<SmartConfiguration> {
    const wellKnownUrl = issUrl.replace(/\/$/, '') + '/.well-known/smart-configuration';
    return this.http.get<SmartConfiguration>(wellKnownUrl);
  }

  /** 以授權碼 / refresh token 交換 token（外部 App 視角，無 admin auth）。 */
  postToken(tokenEndpoint: string, form: Record<string, string>): Observable<SmartTokenResponse> {
    const headers = new HttpHeaders({ 'Content-Type': FORM_URLENCODED });
    return this.http.post<SmartTokenResponse>(tokenEndpoint, toFormBody(form), { headers });
  }

  /** 用 SMART App 自身的 access token 抓 Patient（非 admin token）。 */
  loadPatientWithToken(url: string, accessToken: string, simError?: string): Observable<Patient> {
    let headers = new HttpHeaders({
      'Authorization': `Bearer ${accessToken}`,
      'Accept': 'application/fhir+json',
    });
    if (simError) headers = headers.set('X-Smart-Sim-Error', simError);
    return this.http.get<Patient>(url, { headers });
  }

  /** 從 bundle.link 找指定 relation */
  static findLink(bundle: Bundle | null | undefined, relation: 'next' | 'previous' | 'prev' | 'self' | 'first' | 'last'): string | null {
    const links = bundle?.link || [];
    for (const l of links) {
      if (l.relation === relation) return l.url;
    }
    return null;
  }

  static nextLink(bundle: Bundle | null | undefined): string | null {
    return SmartFhirService.findLink(bundle, 'next');
  }

  static prevLink(bundle: Bundle | null | undefined): string | null {
    return SmartFhirService.findLink(bundle, 'previous') || SmartFhirService.findLink(bundle, 'prev');
  }

  private toRelative(url: string): string {
    try {
      const u = new URL(url, window.location.origin);
      return u.pathname + (u.search || '');
    } catch {
      return url;
    }
  }

  static extractPatientDisplay(resource: Patient): PatientDisplay {
    const id = resource.id || '';
    const names: HumanName[] = resource.name || [];
    const officialName: HumanName = names.find((n) => n.use === 'official') || names[0] || {};
    const family = officialName.family || '';
    const given = (officialName.given || []).join(' ');
    const displayName = officialName.text || [given, family].filter(Boolean).join(' ') || id;

    let age: number | null = null;
    if (resource.birthDate) {
      const diff = Date.now() - new Date(resource.birthDate).getTime();
      age = Math.floor(diff / (365.25 * 24 * 60 * 60 * 1000));
    }

    return {
      id,
      name: displayName,
      gender: resource.gender || '',
      birthDate: resource.birthDate || '',
      age,
    };
  }

  static extractPractitionerDisplay(resource: Practitioner): PractitionerDisplay {
    const id = resource.id || '';
    const names: HumanName[] = resource.name || [];
    const officialName: HumanName = names.find((n) => n.use === 'official') || names[0] || {};
    const family = officialName.family || '';
    const given = (officialName.given || []).join(' ');
    const displayName = officialName.text || [given, family].filter(Boolean).join(' ') || id;

    return {
      id,
      name: displayName,
    };
  }

  static extractEncounterDisplay(resource: Encounter): EncounterDisplay {
    const types: CodeableConcept[] = resource.type || [];
    const typeText = types.map((t) => {
      const codings = t.coding || [];
      return t.text || (codings[0] && codings[0].display) || (codings[0] && codings[0].code) || '';
    }).filter(Boolean).join(', ') || '-';

    let period = '';
    if (resource.period) {
      const start = resource.period.start ? resource.period.start.substring(0, 10) : '';
      const end = resource.period.end ? resource.period.end.substring(0, 10) : '';
      period = end && end !== start ? `${start} ~ ${end}` : start;
    }

    return {
      id: resource.id || '',
      type: typeText,
      period,
      status: resource.status || '',
    };
  }
}

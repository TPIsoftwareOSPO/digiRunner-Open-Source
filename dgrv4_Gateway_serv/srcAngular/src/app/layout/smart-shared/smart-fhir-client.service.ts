import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ToolService } from 'src/app/shared/services/tool.service';

export interface PatientDisplay {
  id: string;
  name: string;
  gender: string;
  birthDate: string;
  age: number | null;
}

export interface ProviderDisplay {
  id: string;
  name: string;
  /** FHIR Reference 形式：Practitioner/{id} */
  reference: string;
}

export interface ConsentInfo {
  clientName: string;
  clientId: string;
  scopes: { scope: string; description: string }[];
  needsPatientSelection: boolean;
  needsEncounterSelection: boolean;
  needsProviderSelection: boolean;
  providerCandidates: string[];
  fhirUser: string | null;
  autoApprove: boolean;
  patientCandidates: string[];
  websiteName: string;
  fhirBasePath: string;
}

export interface PatientById {
  id: string;
  display: PatientDisplay;
}

export interface SmartSelection {
  patientId?: string;
  fhirUser?: string;
  encounterId?: string;
}

/**
 * SMART Launch 流程跨頁面共用 service。
 *
 * 提供：
 * - consent-info 取得
 * - FHIR Patient / Practitioner 搜尋（含名稱與 ID 兩種模式）
 * - sessionStorage 暫存選取結果（在三個頁面間流轉）
 */
@Injectable({ providedIn: 'root' })
export class SmartFhirClientService {

  constructor(private http: HttpClient, private tool: ToolService) {}

  // ==================== consent-info ====================

  getConsentInfo(state: string): Observable<ConsentInfo> {
    return this.http.get<ConsentInfo>('/dgrv4/ssotoken/smart/consent-info', {
      params: { state }
    });
  }

  // ==================== FHIR headers ====================

  fhirHeaders(): HttpHeaders {
    const token = this.tool.getToken();
    let headers = new HttpHeaders({ 'X-Smart-Launcher': 'true' });
    if (token) {
      headers = headers.set('Authorization', `Bearer ${token}`);
    }
    return headers;
  }

  /** 把 fhirBasePath 正規化：'/' 視為空字串避免雙斜線 */
  private normalizeBasePath(fhirBasePath: string): string {
    return (!fhirBasePath || fhirBasePath === '/') ? '' : fhirBasePath;
  }

  // ==================== Patient ====================

  searchPatients(
    websiteName: string, fhirBasePath: string,
    keyword: string, searchBy: 'name' | 'id',
    offset: number, count: number
  ): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Patient`;
    const params: Record<string, string> = {
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    if (keyword) {
      params[searchBy === 'id' ? '_id' : 'name:contains'] = keyword;
    }
    return this.http.get<any>(url, { params, headers: this.fhirHeaders() });
  }

  extractPatientDisplay(resource: any): PatientDisplay {
    const names = resource.name || [];
    const officialName = names.find((n: any) => n.use === 'official') || names[0] || {};
    const family = officialName.family || '';
    const given = (officialName.given || []).join(' ');
    const displayName = officialName.text || [given, family].filter(Boolean).join(' ') || resource.id;

    let age: number | null = null;
    if (resource.birthDate) {
      const diff = Date.now() - new Date(resource.birthDate).getTime();
      age = Math.floor(diff / (365.25 * 24 * 60 * 60 * 1000));
    }
    return {
      id: resource.id,
      name: displayName,
      gender: resource.gender || '',
      birthDate: resource.birthDate || '',
      age,
    };
  }

  // ==================== Practitioner ====================

  searchPractitioners(
    websiteName: string, fhirBasePath: string,
    keyword: string, searchBy: 'name' | 'id',
    count: number, offset: number = 0
  ): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Practitioner`;
    const params: Record<string, string> = {
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    if (keyword) {
      params[searchBy === 'id' ? '_id' : 'name:contains'] = keyword;
    }
    return this.http.get<any>(url, { params, headers: this.fhirHeaders() });
  }

  getPractitionerById(websiteName: string, fhirBasePath: string, id: string): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Practitioner/${id}`;
    return this.http.get<any>(url, { headers: this.fhirHeaders() });
  }

  getPatientById(websiteName: string, fhirBasePath: string, id: string): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Patient/${id}`;
    return this.http.get<any>(url, { headers: this.fhirHeaders() });
  }

  /**
   * 用 FHIR Bundle.link 的 URL（next/previous）直接抓下一頁。
   * 絕對 URL 會被截成相對路徑，避免 CORS 與多餘 origin。
   */
  fetchByLink(url: string): Observable<any> {
    return this.http.get<any>(this.toRelative(url), { headers: this.fhirHeaders() });
  }

  /** 從 bundle.link 找指定 relation 的 url */
  static findLink(bundle: any, relation: 'next' | 'previous' | 'prev' | 'self' | 'first' | 'last'): string | null {
    const links = bundle?.link || [];
    for (const l of links) {
      if (l.relation === relation) return l.url;
    }
    return null;
  }

  /** 找下一頁 URL（next） */
  static nextLink(bundle: any): string | null {
    return SmartFhirClientService.findLink(bundle, 'next');
  }

  /** 找上一頁 URL（previous 或 prev） */
  static prevLink(bundle: any): string | null {
    return SmartFhirClientService.findLink(bundle, 'previous') || SmartFhirClientService.findLink(bundle, 'prev');
  }

  private toRelative(url: string): string {
    try {
      const u = new URL(url, window.location.origin);
      return u.pathname + (u.search || '');
    } catch {
      return url;
    }
  }

  extractProviderDisplay(resource: any): ProviderDisplay {
    const names = resource.name || [];
    const officialName = names.find((n: any) => n.use === 'official') || names[0] || {};
    const family = officialName.family || '';
    const given = (officialName.given || []).join(' ');
    const displayName = officialName.text || [given, family].filter(Boolean).join(' ') || resource.id;
    return {
      id: resource.id,
      name: displayName,
      reference: `Practitioner/${resource.id}`,
    };
  }

  // ==================== Selection sessionStorage ====================

  /** 跨頁暫存選取，依 state 隔離避免同瀏覽器多 tab 互相干擾 */
  private selectionKey(state: string): string {
    return `smart-selection-${state}`;
  }

  loadSelection(state: string): SmartSelection {
    try {
      const raw = sessionStorage.getItem(this.selectionKey(state));
      return raw ? JSON.parse(raw) : {};
    } catch {
      return {};
    }
  }

  saveSelection(state: string, selection: SmartSelection): void {
    const current = this.loadSelection(state);
    const merged = { ...current, ...selection };
    sessionStorage.setItem(this.selectionKey(state), JSON.stringify(merged));
  }

  clearSelection(state: string): void {
    sessionStorage.removeItem(this.selectionKey(state));
  }
}

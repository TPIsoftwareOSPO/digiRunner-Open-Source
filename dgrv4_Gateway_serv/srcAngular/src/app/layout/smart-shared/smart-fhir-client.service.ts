import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { SMART_MISSING_STATE } from './smart-error';

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

export interface EncounterDisplay {
  id: string;
  type: string;
  period: string;
  status: string;
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
  /** 已預綁的病患 ID（launch 註冊時單選帶入；供 encounter 選取頁過濾） */
  patientId: string | null;
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

  /**
   * TokenInterceptor 逃生門標記。
   * 攔截器只判斷 header 是否存在（值不拘），且會在送出前將它 delete，後端看不到。
   */
  private static readonly BYPASS_MARK = 'smart picker';

  constructor(private http: HttpClient) {}

  // ==================== consent-info ====================

  getConsentInfo(state: string): Observable<ConsentInfo> {
    // 非 proxy 路徑，不需要 X-Smart-State，但同樣需要逃生門：
    // state 失效時後端回 400，若不繞過攔截器會彈出顯示原始 JSON 的全域錯誤視窗；
    // 離線時 err.error 為 null 更會讓攔截器內部丟 TypeError。
    return this.http.get<ConsentInfo>('/dgrv4/ssotoken/smart/consent-info', {
      params: { state },
      headers: new HttpHeaders({ digiRunner: SmartFhirClientService.BYPASS_MARK }),
    });
  }

  // ===== Proxy 瀏覽（launch 流程 patient/provider picker）=====
  /**
   * SMART picker 專用的 proxy GET。兩個 header 缺一不可：
   *
   * - digiRunner：TokenInterceptor 逃生門。外部授權流程的使用者沒有後台 refresh_token，
   *   少了它，任何 401 都會讓攔截器清空 sessionStorage 並硬跳轉到後台登入頁，
   *   SMART 流程直接斷掉無法復原。
   * - X-Smart-State：後端 isSmartSessionBypass 的旁路憑證。後端限制為僅 GET、
   *   session phase 須為 AUTHENTICATED、websiteName 須綁定該 proxy、30 分鐘 TTL，
   *   並在轉發前 strip 掉所有 x-smart-* header，不會外洩到 FHIR server。
   */
  private proxyGet<T>(state: string, url: string, params?: Record<string, string>): Observable<T> {
    if (!state) {
      // 各 picker 都在 state 存在時才呼叫，正常走不到這裡。
      // 防呆用哨兵錯誤而非靜默送出無 state 的請求：否則前端 bug 會回 401，
      // 在畫面上長得跟「授權已逾時」一模一樣，難以辨別。
      return throwError(() => new Error(SMART_MISSING_STATE));
    }
    const headers = new HttpHeaders({
      digiRunner: SmartFhirClientService.BYPASS_MARK,
      'X-Smart-State': state,
    });
    return this.http.get<T>(url, { params, headers });
  }

  /** 把 fhirBasePath 正規化：'/' 視為空字串避免雙斜線 */
  private normalizeBasePath(fhirBasePath: string): string {
    return (!fhirBasePath || fhirBasePath === '/') ? '' : fhirBasePath;
  }

  // ==================== Patient ====================

  searchPatients(
    state: string,
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
    return this.proxyGet<any>(state, url, params);
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
    state: string,
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
    return this.proxyGet<any>(state, url, params);
  }

  getPractitionerById(state: string, websiteName: string, fhirBasePath: string, id: string): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Practitioner/${id}`;
    return this.proxyGet<any>(state, url);
  }

  getPatientById(state: string, websiteName: string, fhirBasePath: string, id: string): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Patient/${id}`;
    return this.proxyGet<any>(state, url);
  }

  // ==================== Encounter ====================

  /** 查詢 Encounter；patientId 有值時以 subject 過濾，空值則不過濾（無 patient context 的邊角情境） */
  searchEncounters(
    state: string,
    websiteName: string, fhirBasePath: string,
    patientId: string, offset: number, count: number
  ): Observable<any> {
    const path = this.normalizeBasePath(fhirBasePath);
    const url = `/smart-on-fhir/${websiteName}${path}/Encounter`;
    const params: Record<string, string> = {
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    if (patientId) {
      params['subject'] = `Patient/${patientId}`;
    }
    return this.proxyGet<any>(state, url, params);
  }

  extractEncounterDisplay(resource: any): EncounterDisplay {
    const types: any[] = resource.type || [];
    const typeText = types.map((t: any) => {
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

  /**
   * 用 FHIR Bundle.link 的 URL（next/previous）直接抓下一頁。
   * 絕對 URL 會被截成相對路徑，避免 CORS 與多餘 origin。
   */
  fetchByLink(state: string, url: string): Observable<any> {
    const relative = this.toRelative(url);
    // proxy 未開啟 URL rewrite 時，Bundle.link 會是上游 FHIR server 的絕對 URL，
    // 截成相對路徑後會打到 gateway 根路徑而非本 proxy。直接視為錯誤，
    // 避免送出注定失敗的請求，也避免其 401 被誤判成「授權已逾時」。
    if (!relative.startsWith('/smart-on-fhir/')) {
      return throwError(() => new Error('link_not_under_proxy'));
    }
    return this.proxyGet<any>(state, relative);
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

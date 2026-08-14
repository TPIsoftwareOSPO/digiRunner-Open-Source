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

  // ===== Proxy 瀏覽（launcher 後台 picker）=====
  // 以後台登入 token + X-Smart-Launcher 打 proxy：後端 isLauncherAdminBypass 略過入站驗證，
  // 並 strip 掉此 admin token、依 proxy 的 relay 設定代取後端 token。
  // 取代原 digiCareManager client_credentials 寫死憑證。
  private proxyGet<T>(url: string, params?: Record<string, string>): Observable<T> {
    // 無後台 token 時不帶 bypass 標頭：避免送出字面 "Bearer null"，
    // 且 isLauncherAdminBypass 本來就需要有效 token 才會成立
    const token = this.toolService.getToken();
    const headers = token
      ? new HttpHeaders({ 'Authorization': `Bearer ${token}`, 'X-Smart-Launcher': 'true' })
      : undefined;
    return this.http.get<T>(url, { params, headers });
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
    return this.proxyGet<Bundle<Patient>>(url, params);
  }

  searchEncounters(proxyName: string, fhirBasePath: string, patientId: string, count: number = 20, offset: number = 0): Observable<Bundle<Encounter>> {
    const url = this.buildFhirUrl(proxyName, fhirBasePath, 'Encounter');
    const params: Record<string, string> = {
      'subject': `Patient/${patientId}`,
      '_count': count.toString(),
      '_offset': offset.toString(),
      '_sort': '_id',
    };
    return this.proxyGet<Bundle<Encounter>>(url, params);
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
    return this.proxyGet<Bundle<Practitioner>>(url, params);
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
    return this.proxyGet<FhirResource>(url);
  }

  /** 用 FHIR Bundle.link 的 URL（next/previous）直接抓下一頁 */
  fetchByLink(url: string): Observable<Bundle> {
    return this.proxyGet<Bundle>(this.toRelative(url));
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
  /**
   * 交換 / 更新 token。
   * @param basicAuth 已 base64 編碼的 "client_id:client_secret"（confidential-symmetric 的 client_secret_basic）；
   *                  傳入時附上 Authorization: Basic header。confidential-symmetric demo 用。
   */
  postToken(tokenEndpoint: string, form: Record<string, string>, basicAuth?: string): Observable<SmartTokenResponse> {
    const headerConfig: Record<string, string> = { 'Content-Type': FORM_URLENCODED };
    if (basicAuth) {
      headerConfig['Authorization'] = 'Basic ' + basicAuth;
    }
    const headers = new HttpHeaders(headerConfig);
    return this.http.post<SmartTokenResponse>(tokenEndpoint, toFormBody(form), { headers });
  }

  /**
   * 解碼 JWT payload 為格式化 JSON 字串（顯示用）。
   * 以 TextDecoder 正確處理 UTF-8——直接 atob() 會把多位元組字元（如中文）當 Latin-1 逐位元組解讀而變亂碼。
   */
  decodeJwtPayload(token: string | undefined): string {
    if (!token) return '';
    try {
      const parts = token.split('.');
      if (parts.length !== 3) return '(not a JWT)';
      let b64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
      b64 += '='.repeat((4 - (b64.length % 4)) % 4); // 補回 base64url 去掉的 padding
      const bytes = Uint8Array.from(atob(b64), c => c.charCodeAt(0));
      const json = new TextDecoder('utf-8').decode(bytes);
      return JSON.stringify(JSON.parse(json), null, 2);
    } catch {
      return '(decode failed)';
    }
  }

  // ==================== confidential-asymmetric（private_key_jwt）demo ====================
  // 說明：asymmetric 的本意是私鑰永不離開 client。以下為 launcher 測試工具的刻意取捨——
  // 在瀏覽器內以私鑰簽 client assertion，私鑰會經欄位 / sessionStorage 曝光。正式第三方請勿如此。

  /** 產生 RSA 金鑰對，回傳可直接填入欄位的公鑰 JWKS 與私鑰 JWK 字串（公私鑰帶同一 kid）。 */
  async generateRsaKeypair(): Promise<{ jwks: string; privateKey: string }> {
    if (!crypto?.subtle) {
      throw new Error('Web Crypto (crypto.subtle) unavailable; requires a secure context (https / localhost)');
    }
    const kp = await crypto.subtle.generateKey(
      { name: 'RSASSA-PKCS1-v1_5', modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: 'SHA-256' },
      true, ['sign', 'verify']);
    const pub = await crypto.subtle.exportKey('jwk', kp.publicKey) as JsonWebKey & { kid?: string; use?: string; alg?: string };
    const priv = await crypto.subtle.exportKey('jwk', kp.privateKey) as JsonWebKey & { kid?: string; alg?: string };
    const kid = 'sample-' + crypto.randomUUID().slice(0, 8);
    pub.kid = kid; pub.use = 'sig'; pub.alg = 'RS256';
    priv.kid = kid; priv.alg = 'RS256';
    return {
      jwks: JSON.stringify({ keys: [pub] }, null, 2),
      privateKey: JSON.stringify(priv, null, 2),
    };
  }

  /**
   * 用私鑰簽 SMART private_key_jwt client assertion。私鑰接受 JWK（以 { 開頭）或 PKCS#8 PEM。
   * @param clientId assertion 的 iss / sub
   * @param audience assertion 的 aud —— 需與後端逐字相符：token 交換用 token 端點、launch 註冊用 launch 端點
   * @param privateKeyRaw 私鑰字串（JWK 或 PEM）
   */
  async signClientAssertion(clientId: string, audience: string, privateKeyRaw: string): Promise<string> {
    const { key, kid } = await this.importSigningKey(privateKeyRaw);
    const header: Record<string, string> = { alg: 'RS256', typ: 'JWT' };
    if (kid) header['kid'] = kid;
    const now = Math.floor(Date.now() / 1000);
    const payload = {
      iss: clientId,
      sub: clientId,
      aud: audience,
      exp: now + 240,   // 4 分鐘，< 後端 5 分鐘上限
      iat: now,
      jti: crypto.randomUUID(),
    };
    const signingInput = this.b64urlString(JSON.stringify(header)) + '.' + this.b64urlString(JSON.stringify(payload));
    const sig = await crypto.subtle.sign({ name: 'RSASSA-PKCS1-v1_5' }, key, new TextEncoder().encode(signingInput));
    return signingInput + '.' + this.b64urlBytes(new Uint8Array(sig));
  }

  private async importSigningKey(raw: string): Promise<{ key: CryptoKey; kid: string | null }> {
    if (!crypto?.subtle) {
      throw new Error('Web Crypto (crypto.subtle) unavailable; requires a secure context (https / localhost)');
    }
    const trimmed = raw.trim();
    const alg = { name: 'RSASSA-PKCS1-v1_5', hash: 'SHA-256' };
    if (trimmed.startsWith('{')) {
      const jwk = JSON.parse(trimmed) as JsonWebKey & { kid?: string };
      const key = await crypto.subtle.importKey('jwk', jwk, alg, false, ['sign']);
      return { key, kid: jwk.kid || null };
    }
    // PKCS#8 PEM
    const der = this.pemToDer(trimmed);
    const key = await crypto.subtle.importKey('pkcs8', der, alg, false, ['sign']);
    return { key, kid: null };
  }

  private pemToDer(pem: string): ArrayBuffer {
    const body = pem
      .replace(/-----BEGIN [^-]+-----/g, '')
      .replace(/-----END [^-]+-----/g, '')
      .replace(/\s+/g, '');
    const binary = atob(body);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) {
      bytes[i] = binary.charCodeAt(i);
    }
    return bytes.buffer;
  }

  private b64urlString(str: string): string {
    return this.b64urlBytes(new TextEncoder().encode(str));
  }

  private b64urlBytes(bytes: Uint8Array): string {
    let binary = '';
    bytes.forEach(b => binary += String.fromCharCode(b));
    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
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

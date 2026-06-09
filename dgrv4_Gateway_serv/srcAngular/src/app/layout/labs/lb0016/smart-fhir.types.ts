/**
 * SMART on FHIR 相關型別定義。
 *
 * 僅涵蓋 SMART Launcher（lb0016）實際使用到的 FHIR 欄位，
 * 非完整 FHIR R4 規格——刻意保持精簡，避免引入過度龐大的型別。
 */

/** FHIR HumanName（Patient / Practitioner 的 name 元素） */
export interface HumanName {
  use?: string;
  text?: string;
  family?: string;
  given?: string[];
}

/** FHIR Coding */
export interface Coding {
  system?: string;
  code?: string;
  display?: string;
}

/** FHIR CodeableConcept */
export interface CodeableConcept {
  text?: string;
  coding?: Coding[];
}

/** FHIR Period */
export interface Period {
  start?: string;
  end?: string;
}

/** FHIR 資源基底——所有資源皆有 resourceType；其餘欄位以 unknown 索引保留彈性 */
export interface FhirResource {
  resourceType: string;
  id?: string;
  [key: string]: unknown;
}

/** FHIR Patient（僅 launcher 用到的欄位） */
export interface Patient extends FhirResource {
  resourceType: 'Patient';
  name?: HumanName[];
  gender?: string;
  birthDate?: string;
}

/** FHIR Practitioner（僅 launcher 用到的欄位） */
export interface Practitioner extends FhirResource {
  resourceType: 'Practitioner';
  name?: HumanName[];
}

/** FHIR Encounter（僅 launcher 用到的欄位） */
export interface Encounter extends FhirResource {
  resourceType: 'Encounter';
  type?: CodeableConcept[];
  period?: Period;
  status?: string;
}

/** FHIR Bundle.link 元素（分頁靠 relation=next/previous 的 url） */
export interface BundleLink {
  relation: string;
  url: string;
}

/** FHIR Bundle.entry 元素 */
export interface BundleEntry<T extends FhirResource = FhirResource> {
  resource?: T;
}

/** FHIR Bundle（search 結果） */
export interface Bundle<T extends FhirResource = FhirResource> {
  resourceType: string;
  type?: string;
  total?: number;
  link?: BundleLink[];
  entry?: BundleEntry<T>[];
}

/** SMART .well-known/smart-configuration discovery 回應（僅取用到的端點） */
export interface SmartConfiguration {
  authorization_endpoint?: string;
  token_endpoint?: string;
  [key: string]: unknown;
}

/** SMART token 端點回應 */
export interface SmartTokenResponse {
  access_token?: string;
  id_token?: string;
  refresh_token?: string;
  token_type?: string;
  expires_in?: number;
  scope?: string;
  patient?: string;
  encounter?: string;
  error?: string;
  error_description?: string;
}

/** /dgrv4/ssotoken/smart/launch 回應 */
export interface SmartLaunchResponse {
  launch?: string;
  error?: string;
  error_description?: string;
}

/** 顯示用 Patient 摘要 */
export interface PatientDisplay {
  id: string;
  name: string;
  gender: string;
  birthDate: string;
  age: number | null;
}

/** 顯示用 Practitioner 摘要 */
export interface PractitionerDisplay {
  id: string;
  name: string;
}

/** 顯示用 Encounter 摘要 */
export interface EncounterDisplay {
  id: string;
  type: string;
  period: string;
  status: string;
}

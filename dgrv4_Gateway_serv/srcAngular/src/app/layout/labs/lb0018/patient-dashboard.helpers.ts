/**
 * lb0018 Tab 2「Patient Dashboard」的純函式與 view model 定義。
 * 做法參考 digiCare twcore-patient-dashboard：
 * 以 Patient/$everything 的結果在前端組出病人摘要、就診時間軸與資源參照樹。
 */
import {
  CodeableConcept,
  FhirResource,
  HumanName,
  Patient,
  Period,
} from '../lb0016/smart-fhir.types';

// ===== 僅本檔使用的 FHIR 欄位型別（比照 smart-fhir.types 的精簡風格）=====

interface Reference {
  reference?: string;
  display?: string;
}

interface Quantity {
  value?: number;
  unit?: string;
  code?: string;
}

interface Identifier {
  type?: CodeableConcept;
  value?: string;
}

interface PatientResource extends FhirResource {
  name?: HumanName[];
  gender?: string;
  birthDate?: string;
  identifier?: Identifier[];
  telecom?: { system?: string; value?: string }[];
  address?: { text?: string }[];
  deceasedBoolean?: boolean;
  deceasedDateTime?: string;
}

interface EncounterResource extends FhirResource {
  class?: { code?: string };
  type?: CodeableConcept[];
  serviceType?: CodeableConcept;
  status?: string;
  period?: Period;
  identifier?: Identifier[];
  reasonCode?: CodeableConcept[];
  diagnosis?: { condition?: Reference }[];
  location?: { location?: Reference }[];
  participant?: { individual?: Reference }[];
  hospitalization?: { dischargeDisposition?: CodeableConcept };
}

interface ObservationResource extends FhirResource {
  code?: CodeableConcept;
  valueQuantity?: Quantity;
  referenceRange?: { low?: Quantity; high?: Quantity }[];
  encounter?: Reference;
}

interface MedicationRequestResource extends FhirResource {
  medicationCodeableConcept?: CodeableConcept;
  medicationReference?: Reference;
  dosageInstruction?: { text?: string }[];
  status?: string;
  encounter?: Reference;
}

interface DiagnosticReportResource extends FhirResource {
  code?: CodeableConcept;
  conclusion?: string;
  status?: string;
  encounter?: Reference;
}

interface MediaResource extends FhirResource {
  type?: CodeableConcept;
  status?: string;
  content?: { url?: string; title?: string; contentType?: string };
  encounter?: Reference;
}

interface CodedResource extends FhirResource {
  code?: CodeableConcept;
}

// ===== View Models =====

/** 病人清單 / 病人摘要顯示用 */
export interface PatientVM {
  id: string;
  name: string;
  gender: string;
  birthDate: string;
  idNumber: string;
  phone: string;
  address: string;
}

/** 資源參照樹節點 */
export interface ResourceTreeNode {
  type: string;
  id: string;
  children: ResourceTreeNode[];
}

/** 樹展開後的可見列（左側過篩資料與右側資源檢視共用同一份） */
export interface VisibleRow {
  key: string;
  type: string;
  id: string;
  summary: string;
  depth: number;
  childCount: number;
  hasChildren: boolean;
}

export interface MedVM {
  key: string;
  medication: string;
  dosage: string;
  status: string;
}

export interface LabVM {
  key: string;
  label: string;
  value: string;
  unit: string;
  referenceRange?: string;
  flag: 'high' | 'low' | null;
}

export interface ReportVM {
  key: string;
  code: string;
  conclusion?: string;
}

export interface MediaVM {
  key: string;
  title: string;
  url?: string;
}

/** 就診時間軸卡片 */
export interface EncounterVM {
  id: string;
  classCode: string;
  department: string;
  serviceText: string;
  status: string;
  start: string;
  end: string;
  // 以下為展開後才組出的明細
  identifier?: string;
  reasonText?: string;
  reasonCode?: string;
  diagnosis?: string;
  locationName?: string;
  practitioner?: string;
  dischargeText?: string;
  meds: MedVM[];
  labs: LabVM[];
  reports: ReportVM[];
  media: MediaVM[];
  detailLoaded: boolean;
  expanded: boolean;
}

/** FHIR code 轉多語系顯示文字的 resolver（由元件以 i18n dict 實作） */
export type CodeLabelFn = (group: string, code: string) => string;

/** encounter class code → 時間軸顏色（參考 digiCare 配色） */
export const ENC_CLASS_COLORS: Record<string, string> = {
  AMB: '#2587e9',
  EMER: '#f0524b',
  IMP: '#01727e',
  ACUTE: '#7c3aed',
  NONAC: '#00824c',
  OBSENC: '#d97706',
  PRENC: '#db2777',
  SS: '#0891b2',
  VR: '#4f46e5',
  HH: '#be185d',
};
export const ENC_CLASS_DEFAULT_COLOR = '#4f4f4f';

// ===== 共用小工具 =====

/** CodeableConcept → 顯示文字 */
export function ccText(cc?: CodeableConcept | null): string {
  return cc?.text || cc?.coding?.[0]?.display || cc?.coding?.[0]?.code || '';
}

const REF_RE = /([A-Za-z]+)\/([A-Za-z0-9\-.]{1,64})(?:\/_history\/[^/]+)?$/;

/** 從 reference 字串取出正規化的 "Type/id"（去除完整 URL 與 _history 版本） */
function canonicalRef(ref?: string): string | null {
  if (!ref) return null;
  const m = ref.match(REF_RE);
  return m ? `${m[1]}/${m[2]}` : null;
}

/** 遞迴收集資源內所有 reference 指到的 "Type/id" */
function collectRefs(value: unknown, out: Set<string>): void {
  if (Array.isArray(value)) {
    value.forEach((v) => collectRefs(v, out));
    return;
  }
  if (value && typeof value === 'object') {
    const ref = (value as { reference?: unknown }).reference;
    if (typeof ref === 'string') {
      const canon = canonicalRef(ref);
      if (canon) out.add(canon);
    }
    Object.values(value).forEach((v) => collectRefs(v, out));
  }
}

// ===== Patient =====

/** 台灣身分證件常見的 identifier type code（比照 digiCare） */
const ID_TYPE_CODES = new Set(['NNTWN', 'NNxxx', 'NI']);

export function toPatientVM(res: Patient): PatientVM {
  const p = res as PatientResource;
  const n = p.name?.[0];
  const name =
    n?.text ||
    [...(n?.given ?? []), n?.family].filter(Boolean).join(' ') ||
    String(p.id ?? '');
  const deceased =
    p.deceasedBoolean === true ||
    (typeof p.deceasedDateTime === 'string' && p.deceasedDateTime.trim() !== '');
  const identifiers = p.identifier ?? [];
  const idNo = identifiers.find((i) =>
    i.type?.coding?.some((c) => c.code != null && ID_TYPE_CODES.has(c.code)),
  );
  return {
    id: String(p.id ?? ''),
    name: deceased ? `${name} (Deceased)` : name,
    gender: String(p.gender ?? ''),
    birthDate: String(p.birthDate ?? ''),
    idNumber: idNo?.value ?? identifiers[0]?.value ?? '',
    phone: (p.telecom ?? [])
      .filter((t) => t.system === 'phone' && t.value)
      .map((t) => t.value)
      .join('、'),
    address: (p.address ?? [])[0]?.text ?? '',
  };
}

export function calcAge(birthDate?: string): number | null {
  if (!birthDate) return null;
  const b = new Date(birthDate);
  if (Number.isNaN(b.getTime())) return null;
  const now = new Date();
  let age = now.getFullYear() - b.getFullYear();
  const m = now.getMonth() - b.getMonth();
  if (m < 0 || (m === 0 && now.getDate() < b.getDate())) age--;
  return age;
}

/** FHIR dateTime → "YYYY-MM-DD" 或 "YYYY-MM-DD HH:mm"（純日期不顯示時間） */
export function formatFhirDate(value?: string): string {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return value;
  const pad = (n: number) => String(n).padStart(2, '0');
  const date = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
  return /^\d{4}-\d{2}-\d{2}$/.test(value) ? date : `${date} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

// ===== 資源一行摘要（過篩資料樹 / 資源檢視卡片標頭用）=====

export function summarize(res: FhirResource, label: CodeLabelFn): string {
  const join = (...parts: (string | undefined)[]) => parts.filter(Boolean).join(' · ');
  switch (res.resourceType) {
    case 'Patient': {
      const vm = toPatientVM(res as Patient);
      return join(vm.name, label('gender', vm.gender));
    }
    case 'Encounter': {
      const e = res as EncounterResource;
      return join(
        ccText(e.serviceType),
        label('enc_class', e.class?.code ?? ''),
        label('enc_status', String(e.status ?? '')),
      );
    }
    case 'Condition':
    case 'Procedure':
    case 'AllergyIntolerance':
    case 'Medication':
      return ccText((res as CodedResource).code);
    case 'Observation': {
      const o = res as ObservationResource;
      const q = o.valueQuantity;
      return join(
        ccText(o.code),
        q?.value != null ? `${q.value} ${q.unit ?? q.code ?? ''}`.trim() : undefined,
      );
    }
    case 'MedicationRequest': {
      const m = res as MedicationRequestResource;
      return join(
        ccText(m.medicationCodeableConcept) || m.dosageInstruction?.[0]?.text,
        label('med_status', String(m.status ?? '')),
      );
    }
    case 'DiagnosticReport': {
      const d = res as DiagnosticReportResource;
      return join(ccText(d.code), String(d.status ?? ''));
    }
    case 'Immunization':
      return ccText((res as { vaccineCode?: CodeableConcept }).vaccineCode);
    case 'Organization':
    case 'Practitioner': {
      const nm = (res as { name?: unknown }).name;
      if (typeof nm === 'string') return nm;
      return (nm as HumanName[] | undefined)?.[0]?.text ?? '';
    }
    case 'PractitionerRole':
      return ccText((res as { code?: CodeableConcept[] }).code?.[0]);
    case 'Media': {
      const m = res as MediaResource;
      return join(ccText(m.type), String(m.status ?? ''));
    }
    default:
      return '';
  }
}

// ===== 資源參照樹（過篩資料）=====

export interface ResourceTreeResult {
  tree: ResourceTreeNode[];
  byKey: Map<string, FhirResource>;
  typeCount: number;
}

/**
 * 依資源間的 reference 組出以 Patient 為根的參照樹（port 自 digiCare）：
 * 1. 直接參照病人的 Encounter 掛在根節點下
 * 2. 直接參照病人的其他資源：若也參照某個已掛上的 Encounter 則掛該 Encounter 下，否則掛根節點
 * 3. 反覆將「被樹上節點參照」的資源掛到該節點下（如 DiagnosticReport.result → Observation）
 * 4. 其餘孤兒資源掛根節點
 */
export function buildResourceTree(
  patientId: string,
  resources: FhirResource[],
): ResourceTreeResult {
  const rootKey = `Patient/${patientId}`;
  const byKey = new Map<string, FhirResource>();
  resources.forEach((r) => {
    if (r.resourceType && r.id != null) byKey.set(`${r.resourceType}/${r.id}`, r);
  });

  // 每個資源向外參照的 key 集合
  const refs = new Map<string, Set<string>>();
  byKey.forEach((res, key) => {
    const s = new Set<string>();
    collectRefs(res, s);
    s.delete(key);
    refs.set(key, s);
  });

  const nodes = new Map<string, ResourceTreeNode>();
  const makeNode = (key: string): ResourceTreeNode => {
    const i = key.indexOf('/');
    const node: ResourceTreeNode = { type: key.slice(0, i), id: key.slice(i + 1), children: [] };
    nodes.set(key, node);
    return node;
  };

  const root = makeNode(rootKey);
  const placed = new Set<string>([rootKey]);
  const others = [...byKey.keys()].filter((k) => k !== rootKey);
  const refsPatient = (k: string) => refs.get(k)?.has(rootKey) ?? false;

  // 1) 直接參照病人的 Encounter
  others.forEach((k) => {
    if (!placed.has(k) && k.startsWith('Encounter/') && refsPatient(k)) {
      root.children.push(makeNode(k));
      placed.add(k);
    }
  });
  // 2) 直接參照病人的其他資源
  others.forEach((k) => {
    if (placed.has(k) || !refsPatient(k)) return;
    const encKey = [...(refs.get(k) ?? [])].find(
      (t) => t.startsWith('Encounter/') && placed.has(t),
    );
    ((encKey && nodes.get(encKey)) || root).children.push(makeNode(k));
    placed.add(k);
  });
  // 3) 反覆將被樹上節點參照的資源掛上
  let changed = true;
  while (changed) {
    changed = false;
    for (const k of others) {
      if (placed.has(k)) continue;
      const parent = [rootKey, ...others].find(
        (t) => placed.has(t) && (refs.get(t)?.has(k) ?? false),
      );
      if (parent) {
        nodes.get(parent)!.children.push(makeNode(k));
        placed.add(k);
        changed = true;
      }
    }
  }
  // 4) 剩餘孤兒資源
  others.forEach((k) => {
    if (!placed.has(k)) {
      root.children.push(makeNode(k));
      placed.add(k);
    }
  });

  return {
    tree: [root],
    byKey,
    typeCount: new Set(resources.map((r) => r.resourceType)).size,
  };
}

/** 依展開狀態攤平出可見列（去重；子節點僅在父節點展開時列出） */
export function flattenVisible(
  tree: ResourceTreeNode[],
  revealed: Set<string>,
  summaryOf: (key: string) => string,
): VisibleRow[] {
  const rows: VisibleRow[] = [];
  const seen = new Set<string>();
  const walk = (nodeList: ResourceTreeNode[], depth: number) => {
    for (const n of nodeList) {
      const key = `${n.type}/${n.id}`;
      if (!seen.has(key)) {
        seen.add(key);
        rows.push({
          key,
          type: n.type,
          id: n.id,
          summary: summaryOf(key),
          depth,
          childCount: n.children.length,
          hasChildren: n.children.length > 0,
        });
      }
      if (revealed.has(key)) walk(n.children, depth + 1);
    }
  };
  walk(tree, 0);
  return rows;
}

// ===== 就診時間軸（病歷摘要）=====

export function toEncounterVM(res: FhirResource): EncounterVM {
  const e = res as EncounterResource;
  return {
    id: String(e.id ?? ''),
    classCode: e.class?.code ?? '',
    department: ccText(e.type?.[0]),
    serviceText: ccText(e.serviceType),
    status: String(e.status ?? ''),
    start: e.period?.start ?? '',
    end: e.period?.end ?? '',
    meds: [],
    labs: [],
    reports: [],
    media: [],
    detailLoaded: false,
    expanded: false,
  };
}

/** 展開就診卡片時，從 $everything 結果組出該次就診的明細（診斷 / 用藥 / 檢驗 / 報告 / 媒體） */
export function fillEncounterDetail(
  vm: EncounterVM,
  byKey: Map<string, FhirResource>,
  resources: FhirResource[],
): void {
  const encKey = `Encounter/${vm.id}`;
  const resolve = (ref?: Reference): FhirResource | undefined => {
    const canon = canonicalRef(ref?.reference);
    return canon ? byKey.get(canon) : undefined;
  };

  const enc = byKey.get(encKey) as EncounterResource | undefined;
  if (enc) {
    vm.identifier = enc.identifier?.[0]?.value;
    const reason = enc.reasonCode?.[0];
    vm.reasonText = reason?.text || reason?.coding?.[0]?.display;
    vm.reasonCode = reason?.coding?.[0]?.code;
    const cond = resolve(enc.diagnosis?.[0]?.condition) as CodedResource | undefined;
    vm.diagnosis = ccText(cond?.code) || vm.reasonText;
    const locRef = enc.location?.[0]?.location;
    vm.locationName =
      (resolve(locRef) as { name?: string } | undefined)?.name || locRef?.display;
    const indRef = enc.participant?.[0]?.individual;
    vm.practitioner =
      (resolve(indRef) as { name?: HumanName[] } | undefined)?.name?.[0]?.text ||
      indRef?.display;
    vm.dischargeText = ccText(enc.hospitalization?.dischargeDisposition) || undefined;
  }

  const ofEnc = <T extends FhirResource & { encounter?: Reference }>(type: string): T[] =>
    resources
      .filter((r) => r.resourceType === type)
      .map((r) => r as T)
      .filter((t) => canonicalRef(t.encounter?.reference) === encKey);

  vm.meds = ofEnc<MedicationRequestResource>('MedicationRequest').map((m) => ({
    key: String(m.id ?? ccText(m.medicationCodeableConcept)),
    medication:
      ccText((resolve(m.medicationReference) as CodedResource | undefined)?.code) ||
      ccText(m.medicationCodeableConcept) ||
      '—',
    dosage: m.dosageInstruction?.[0]?.text || '—',
    status: String(m.status ?? ''),
  }));

  vm.labs = ofEnc<ObservationResource>('Observation').map((o) => {
    const q = o.valueQuantity;
    const rr = o.referenceRange?.[0];
    const low = rr?.low?.value;
    const high = rr?.high?.value;
    let flag: 'high' | 'low' | null = null;
    if (q?.value != null) {
      if (high != null && q.value > high) flag = 'high';
      else if (low != null && q.value < low) flag = 'low';
    }
    return {
      key: String(o.id ?? ccText(o.code)),
      label: ccText(o.code) || 'Observation',
      value: q?.value != null ? String(q.value) : '—',
      unit: q?.unit ?? q?.code ?? '',
      referenceRange: low != null || high != null ? `${low ?? ''} – ${high ?? ''}` : undefined,
      flag,
    };
  });

  vm.reports = ofEnc<DiagnosticReportResource>('DiagnosticReport').map((d) => ({
    key: String(d.id ?? ccText(d.code)),
    code: ccText(d.code) || 'DiagnosticReport',
    conclusion: d.conclusion,
  }));

  vm.media = ofEnc<MediaResource>('Media').map((md) => ({
    key: String(md.id ?? md.content?.url ?? 'media'),
    title: md.content?.title || mediaFileName(md.content?.url),
    url: md.content?.url,
  }));
}

/** 從 media URL 取出檔名作為顯示標題 */
function mediaFileName(url?: string): string {
  if (!url) return 'Media';
  const path = url.split('?')[0];
  const name = path.substring(path.lastIndexOf('/') + 1);
  try {
    return decodeURIComponent(name) || 'Media';
  } catch {
    return name || 'Media';
  }
}

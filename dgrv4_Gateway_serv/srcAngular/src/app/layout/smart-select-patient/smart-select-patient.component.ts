import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, PatientDisplay } from '../smart-shared/smart-fhir-client.service';
import { classifySmartError, SmartErrorKind, SmartPanelStatus, SMART_MESSAGE_KEYS } from '../smart-shared/smart-error';

type ViewState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-smart-select-patient',
  templateUrl: './smart-select-patient.component.html',
  styleUrls: ['./smart-select-patient.component.scss'],
  standalone: false,
})
export class SmartSelectPatientComponent implements OnInit {

  state: string = '';
  viewState: ViewState = 'loading';
  /** viewState==='error' 時決定顯示哪一段說明 */
  errorKind: SmartErrorKind = 'server';
  /** 清單層（非致命）錯誤：有值時清單區顯示「查詢失敗」而非「找不到病患」 */
  listError: SmartErrorKind | null = null;

  clientName: string = '';
  websiteName: string = '';
  fhirBasePath: string = '';

  searchMode: 'name' | 'id' = 'name';
  keyword: string = '';
  patients: PatientDisplay[] = [];
  selected: PatientDisplay | null = null;
  loading: boolean = false;
  page: number = 1;
  nextUrl: string | null = null;
  prevUrl: string | null = null;
  readonly pageSize: number = 20;

  /** 候選清單（多選時帶來的限定範圍）；空陣列代表全域搜尋 */
  candidates: string[] = [];
  /** 候選 id → display 預載對應表（受限模式時） */
  candidateDisplayMap: Record<string, PatientDisplay> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fhir: SmartFhirClientService,
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const state = params['state'];
      if (!state) {
        this.errorKind = 'no-state';
        this.viewState = 'error';
        return;
      }
      this.state = state;

      // 取得 consent-info 拿 website / fhirBasePath / clientName
      this.fhir.getConsentInfo(state).subscribe({
        next: (info: ConsentInfo) => {
          // dispatcher 防呆：若其實不需要 patient 選取，回 dispatcher 重新派發
          if (!info.needsPatientSelection) {
            this.router.navigate(['/smart/consent'], { queryParams: { state } });
            return;
          }
          this.clientName = info.clientName;
          this.websiteName = info.websiteName ?? '';
          this.fhirBasePath = info.fhirBasePath ?? '';
          this.candidates = info.patientCandidates ?? [];
          this.viewState = 'ready';
          // 預載：把 sessionStorage 中已選的 patientId 帶入 selected（讓 Back 回來不空白）
          const sel = this.fhir.loadSelection(state);
          if (sel.patientId) {
            this.selected = { id: sel.patientId, name: sel.patientId, gender: '', birthDate: '', age: null };
          }
          if (this.candidates.length > 0) {
            // 受限模式：預載候選人顯示資訊，不做全域搜尋
            this.preloadCandidates();
          } else {
            // 全域搜尋
            this.search();
          }
        },
        error: (err) => {
          this.errorKind = classifySmartError(err);
          this.viewState = 'error';
        },
      });
    });
  }

  search(): void {
    this.page = 1;
    this.nextUrl = null;
    this.prevUrl = null;
    this.loading = true;
    this.fhir.searchPatients(this.state, this.websiteName, this.fhirBasePath, this.keyword, this.searchMode, 0, this.pageSize)
      .subscribe({
        next: (bundle) => this.applyBundle(bundle),
        error: (err) => this.handleQueryError(err),
      });
  }

  prevPage(): void {
    if (!this.prevUrl) return;
    this.loading = true;
    this.fhir.fetchByLink(this.state, this.prevUrl).subscribe({
      next: (bundle) => {
        this.page = Math.max(1, this.page - 1);
        this.applyBundle(bundle);
      },
      error: (err) => this.handleQueryError(err),
    });
  }

  nextPage(): void {
    if (!this.nextUrl) return;
    this.loading = true;
    this.fhir.fetchByLink(this.state, this.nextUrl).subscribe({
      next: (bundle) => {
        this.page += 1;
        this.applyBundle(bundle);
      },
      error: (err) => this.handleQueryError(err),
    });
  }

  /**
   * FHIR 查詢失敗的統一處理。
   * state 失效時整個授權流程已無法復原（留在清單頁重試永遠不會成功），故升級為整頁錯誤；
   * 網路／後端錯誤則只讓清單區顯示錯誤並保留重試，不中斷流程。
   */
  private handleQueryError(err: unknown): void {
    this.loading = false;
    const kind = classifySmartError(err);
    if (!this.escalateIfFatal(kind)) {
      this.listError = kind;
    }
  }

  /** state 失效時把整頁切成錯誤頁；回傳是否已升級。多次呼叫等冪。 */
  private escalateIfFatal(kind: SmartErrorKind): boolean {
    if (kind === 'expired' || kind === 'no-state') {
      this.errorKind = kind;
      this.viewState = 'error';
      return true;
    }
    return false;
  }

  get panelStatus(): SmartPanelStatus {
    return this.viewState === 'loading' ? 'loading' : this.errorKind;
  }

  /** 清單內聯錯誤的次要說明 i18n key */
  get listErrorHint(): string {
    return this.listError ? (SMART_MESSAGE_KEYS[this.listError].sub ?? '') : '';
  }

  private applyBundle(bundle: any): void {
    this.listError = null;
    const entries = bundle.entry || [];
    this.patients = entries.map((e: any) => this.fhir.extractPatientDisplay(e.resource));
    this.nextUrl = SmartFhirClientService.nextLink(bundle);
    this.prevUrl = SmartFhirClientService.prevLink(bundle);
    this.loading = false;
  }

  get hasNextPage(): boolean { return !!this.nextUrl; }
  get hasPrevPage(): boolean { return !!this.prevUrl; }

  pick(p: PatientDisplay): void {
    this.selected = p;
  }

  pickCandidate(id: string): void {
    this.selected = this.candidateDisplayMap[id] || { id, name: id, gender: '', birthDate: '', age: null };
  }

  confirm(): void {
    if (!this.selected) return;
    this.fhir.saveSelection(this.state, { patientId: this.selected.id });
    this.router.navigate(['/smart/consent'], { queryParams: { state: this.state } });
  }

  /** 受限模式：把每個候選 ID 對應的 Patient resource 抓回來 */
  private preloadCandidates(): void {
    this.candidates.forEach(id => {
      this.fhir.getPatientById(this.state, this.websiteName, this.fhirBasePath, id).subscribe({
        next: (resource) => {
          if (resource?.resourceType === 'Patient') {
            this.candidateDisplayMap[id] = this.fhir.extractPatientDisplay(resource);
          }
        },
        error: (err) => {
          // 單筆查不到仍保留 ID 當顯示 fallback，不讓整頁掛掉；
          // 但 state 失效時整批都會失敗，直接升級為整頁錯誤。
          this.candidateDisplayMap[id] = { id, name: id, gender: '', birthDate: '', age: null };
          this.escalateIfFatal(classifySmartError(err));
        },
      });
    });
  }

  get isRestricted(): boolean {
    return this.candidates.length > 0;
  }
}

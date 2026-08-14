import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, ProviderDisplay } from '../smart-shared/smart-fhir-client.service';
import { classifySmartError, SmartErrorKind, SmartPanelStatus, SMART_MESSAGE_KEYS } from '../smart-shared/smart-error';

type ViewState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-smart-select-provider',
  templateUrl: './smart-select-provider.component.html',
  styleUrls: ['./smart-select-provider.component.scss'],
  standalone: false,
})
export class SmartSelectProviderComponent implements OnInit {

  state: string = '';
  viewState: ViewState = 'loading';
  /** viewState==='error' 時決定顯示哪一段說明 */
  errorKind: SmartErrorKind = 'server';
  /** 清單層（非致命）錯誤：有值時清單區顯示「查詢失敗」而非「找不到醫療人員」 */
  listError: SmartErrorKind | null = null;

  clientName: string = '';
  websiteName: string = '';
  fhirBasePath: string = '';

  /** 候選清單，FHIR Reference 格式（Practitioner/{id}） */
  candidates: string[] = [];
  /** reference → display 對應表（受限模式預載；全域搜尋逐步填入） */
  displayMap: Record<string, ProviderDisplay> = {};

  /** 受限模式：使用者勾選的 reference */
  selectedRef: string = '';

  // 全域搜尋模式（候選 = 0）
  searchMode: 'name' | 'id' = 'name';
  keyword: string = '';
  results: ProviderDisplay[] = [];
  loading: boolean = false;
  page: number = 1;
  nextUrl: string | null = null;
  prevUrl: string | null = null;
  readonly pageSize: number = 20;

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

      this.fhir.getConsentInfo(state).subscribe({
        next: (info: ConsentInfo) => {
          if (!info.needsProviderSelection) {
            this.router.navigate(['/smart/consent'], { queryParams: { state } });
            return;
          }
          this.clientName = info.clientName;
          this.websiteName = info.websiteName ?? '';
          this.fhirBasePath = info.fhirBasePath ?? '';
          this.candidates = info.providerCandidates ?? [];
          this.viewState = 'ready';
          // 預載：把 sessionStorage 中已選的 fhirUser 帶入 selectedRef
          const sel = this.fhir.loadSelection(state);
          if (sel.fhirUser) {
            this.selectedRef = sel.fhirUser;
          }

          if (this.candidates.length > 0) {
            this.preloadCandidates();
          } else {
            // 全域搜尋模式：自動觸發第一次搜尋
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

  /** 受限模式（候選 2+）：預載候選顯示名稱 */
  private preloadCandidates(): void {
    this.candidates.forEach(reference => {
      const id = reference.startsWith('Practitioner/')
        ? reference.substring('Practitioner/'.length)
        : reference;
      this.fhir.getPractitionerById(this.state, this.websiteName, this.fhirBasePath, id).subscribe({
        next: (resource) => {
          if (resource?.resourceType === 'Practitioner') {
            this.displayMap[reference] = this.fhir.extractProviderDisplay(resource);
          } else {
            this.displayMap[reference] = { id, name: id, reference };
          }
        },
        error: (err) => {
          // 單筆查不到仍保留 ID 當顯示 fallback，不讓整頁掛掉；
          // 但 state 失效時整批都會失敗，直接升級為整頁錯誤。
          this.displayMap[reference] = { id, name: id, reference };
          this.escalateIfFatal(classifySmartError(err));
        },
      });
    });
  }

  /** 全域搜尋模式（候選 0） */
  search(): void {
    this.page = 1;
    this.nextUrl = null;
    this.prevUrl = null;
    this.loading = true;
    this.fhir.searchPractitioners(this.state, this.websiteName, this.fhirBasePath, this.keyword, this.searchMode, this.pageSize, 0)
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
    this.results = entries
      .filter((e: any) => e.resource?.resourceType === 'Practitioner')
      .map((e: any) => this.fhir.extractProviderDisplay(e.resource));
    this.nextUrl = SmartFhirClientService.nextLink(bundle);
    this.prevUrl = SmartFhirClientService.prevLink(bundle);
    this.loading = false;
  }

  get hasNextPage(): boolean { return !!this.nextUrl; }
  get hasPrevPage(): boolean { return !!this.prevUrl; }

  pickCandidate(reference: string): void {
    this.selectedRef = reference;
  }

  pickResult(p: ProviderDisplay): void {
    this.selectedRef = p.reference;
    this.displayMap[p.reference] = p;
  }

  getLabel(reference: string): string {
    const d = this.displayMap[reference];
    return d ? `${d.name} (${d.id})` : reference;
  }

  get isRestricted(): boolean {
    return this.candidates.length > 0;
  }

  confirm(): void {
    if (!this.selectedRef) return;
    this.fhir.saveSelection(this.state, { fhirUser: this.selectedRef });
    this.router.navigate(['/smart/consent'], { queryParams: { state: this.state } });
  }
}

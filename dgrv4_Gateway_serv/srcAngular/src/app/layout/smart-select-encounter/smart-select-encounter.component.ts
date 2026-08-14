import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SmartFhirClientService, ConsentInfo, EncounterDisplay } from '../smart-shared/smart-fhir-client.service';
import { classifySmartError, SmartErrorKind, SmartPanelStatus, SMART_MESSAGE_KEYS } from '../smart-shared/smart-error';

type ViewState = 'loading' | 'ready' | 'error';

@Component({
  selector: 'app-smart-select-encounter',
  templateUrl: './smart-select-encounter.component.html',
  styleUrls: ['./smart-select-encounter.component.scss'],
  standalone: false,
})
export class SmartSelectEncounterComponent implements OnInit {

  state: string = '';
  viewState: ViewState = 'loading';
  /** viewState==='error' 時決定顯示哪一段說明 */
  errorKind: SmartErrorKind = 'server';
  /** 清單層（非致命）錯誤：有值時清單區顯示「查詢失敗」而非「找不到就醫紀錄」 */
  listError: SmartErrorKind | null = null;

  clientName: string = '';
  websiteName: string = '';
  fhirBasePath: string = '';

  /** 過濾 encounter 用的病患 ID：優先取 consent 流程剛選的，其次取 launch 預綁的 */
  patientId: string = '';

  encounters: EncounterDisplay[] = [];
  selected: EncounterDisplay | null = null;
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

      // 取得 consent-info 拿 website / fhirBasePath / clientName / 預綁病患
      this.fhir.getConsentInfo(state).subscribe({
        next: (info: ConsentInfo) => {
          // dispatcher 防呆：若其實不需要 encounter 選取，回 dispatcher 重新派發
          if (!info.needsEncounterSelection) {
            this.router.navigate(['/smart/consent'], { queryParams: { state } });
            return;
          }
          this.clientName = info.clientName;
          this.websiteName = info.websiteName ?? '';
          this.fhirBasePath = info.fhirBasePath ?? '';
          this.viewState = 'ready';
          const sel = this.fhir.loadSelection(state);
          // consent 流程剛選的病患存在 sessionStorage；launch 預綁的由 consent-info 提供
          this.patientId = sel.patientId || info.patientId || '';
          // 預載：把已選的 encounterId 帶入 selected（讓 Back 回來不空白）
          if (sel.encounterId) {
            this.selected = { id: sel.encounterId, type: sel.encounterId, period: '', status: '' };
          }
          this.search();
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
    this.fhir.searchEncounters(this.state, this.websiteName, this.fhirBasePath, this.patientId, 0, this.pageSize)
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
    this.encounters = entries.map((e: any) => this.fhir.extractEncounterDisplay(e.resource));
    this.nextUrl = SmartFhirClientService.nextLink(bundle);
    this.prevUrl = SmartFhirClientService.prevLink(bundle);
    this.loading = false;
  }

  get hasNextPage(): boolean { return !!this.nextUrl; }
  get hasPrevPage(): boolean { return !!this.prevUrl; }

  pick(e: EncounterDisplay): void {
    this.selected = e;
  }

  confirm(): void {
    if (!this.selected) return;
    this.fhir.saveSelection(this.state, { encounterId: this.selected.id });
    this.router.navigate(['/smart/consent'], { queryParams: { state: this.state } });
  }
}

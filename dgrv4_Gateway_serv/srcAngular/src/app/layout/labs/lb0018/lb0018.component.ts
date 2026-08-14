import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import {
  FormsModule,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup,
} from '@angular/forms';
import { ConfirmationService } from 'primeng/api';
import { PaginatorState } from 'primeng/paginator';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { of, Subject } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  finalize,
  map,
  switchMap,
  takeUntil,
} from 'rxjs/operators';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { PrimengModule } from 'src/app/shared/primeng.module';
import { SharedModule } from 'src/app/shared/shared.module';
import { SharedPipesModule } from 'src/app/shared/pipes/shared-pipes.module';
import { TokenExpiredGuard } from 'src/app/shared';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { KeyValueComponent } from 'src/app/shared/key-value/key-value.module';
import { KeyValueGridComponent } from 'src/app/shared/key-value-grid/key-value-grid.component';
import { CommonAPI } from 'src/app/shared/register-api/common-api.class';
import { HttpHeader, FormParam } from 'src/app/models/api/ApiService/aa0312_v3.interface';
import { TranslateService } from '@ngx-translate/core';
import {
  SmartFhirService,
  SmartConfiguration,
} from '../lb0016/smart-fhir.service';
import {
  Bundle,
  FhirResource,
  Patient,
} from '../lb0016/smart-fhir.types';
import {
  PatientVM,
  EncounterVM,
  VisibleRow,
  ResourceTreeNode,
  ENC_CLASS_COLORS,
  ENC_CLASS_DEFAULT_COLOR,
  toPatientVM,
  calcAge,
  formatFhirDate,
  summarize,
  buildResourceTree,
  flattenVisible,
  toEncounterVM,
  fillEncounterDetail,
} from './patient-dashboard.helpers';
import { environment } from 'src/environments/environment';

/** 病人搜尋條件；每個條件對應一個固定的 FHIR 搜尋參數（見 PATIENT_SEARCH_PARAM） */
export type PatientSearchMode = 'name' | 'identifier' | 'id';

/** 搜尋條件 → FHIR Patient 搜尋參數對照 */
const PATIENT_SEARCH_PARAM: Record<PatientSearchMode, string> = {
  name: 'name:contains',
  identifier: 'identifier',
  id: '_id',
};

@Component({
  selector: 'app-lb0018',
  templateUrl: './lb0018.component.html',
  styleUrls: ['./lb0018.component.css'],
  providers: [ConfirmationService, TokenExpiredGuard],
  imports: [
    CommonModule,
    PrimengModule,
    SharedModule,
    SharedPipesModule,
    ReactiveFormsModule,
    FormsModule,
  ],
  standalone: true,
})
export class Lb0018Component extends BaseComponent implements OnInit, OnDestroy {
  @ViewChild('keyValueRequest') kv_request!: KeyValueComponent;
  @ViewChild('keyValueForm') kv_form!: KeyValueComponent | KeyValueGridComponent;

  currentTitle = this.title;
  form!: UntypedFormGroup;

  // Smart On FHIR Proxy 下拉選項（proxy × diversion 展開後去重的 URL 清單）
  proxyUrlOptions: string[] = [];

  // 驗證結果旗標（小紅字顯示用）
  baseUrlNotFound: boolean = false;
  wellKnownNotFound: boolean = false;

  // 查回的 .well-known/smart-configuration 內容
  smartConfig: SmartConfiguration | null = null;

  // Authorization 分頁取回的 access_token（供 Tab 2 Patient Dashboard 的 FHIR 查詢帶入 Bearer）
  accessToken: string | null = null;
  // access_token 對應的 token_type（OAuth 回應提供，預設 Bearer）
  tokenType: string = 'Bearer';

  // 分頁共用區目前顯示的 tab
  activeTab: 'authorization' | 'tab2' = 'authorization';

  // ===== Tab 2：Patient Dashboard（做法參考 digiCare twcore-patient-dashboard）=====
  /** Patient 清單每頁筆數（_count） */
  readonly patientPageSize = 20;
  /** MedicationRequest status → pill 樣式 class */
  private static readonly MED_STATUS_CLS: Record<string, string> = {
    active: 'pd-pill--green',
    completed: 'pd-pill--gray',
    stopped: 'pd-pill--red',
    'on-hold': 'pd-pill--amber',
  };

  // 病人搜尋清單（累積載入，「載入更多」往後翻頁）
  patients: PatientVM[] = [];
  patientTotal: number | null = null;
  patientsFailed = false;
  patientsLoaded = false;
  patientsLoadingMore = false;
  hasMorePatients = false;
  private patientOffset = 0;
  /** 按下搜尋時鎖定的關鍵字（清單累積載入期間不受輸入框變動影響） */
  private patientQuery = '';
  /** 按下搜尋時鎖定的搜尋條件（對應 FHIR 搜尋參數，同上不受 radio 變動影響） */
  private patientQueryMode: PatientSearchMode = 'name';
  /** 上次載入病人清單所用的 base URL，切回 tab 時比對決定是否需要重載 */
  private lastPatientListBase = '';

  // 已選病人與 $everything 結果（分頁載入，每次 everythingPageSize 筆）
  selectedPatient: PatientVM | null = null;
  viewMode: 'summary' | 'resource' | 'json' = 'summary';
  everythingFailed = false;
  everythingLoaded = false;
  everythingEntries: FhirResource[] = [];
  everythingJson = '';
  resourceTypeCount = 0;
  /** Patient $everything 每頁筆數（_count / paginator rows） */
  readonly everythingPageSize = 20;
  /** 目前頁的起始位移（_offset / paginator first 綁定） */
  everythingFirst = 0;
  /** $everything 總筆數（paginator totalRecords；null 表示伺服器未回傳 total） */
  everythingTotal: number | null = null;

  // 病歷摘要（就診時間軸）
  encounters: EncounterVM[] = [];

  // 過篩資料樹與資源檢視（共用同一份可見列）
  private tree: ResourceTreeNode[] = [];
  private byKey = new Map<string, FhirResource>();
  revealed = new Set<string>();
  visibleRows: VisibleRow[] = [];
  expandedCards = new Set<string>();
  private cardJsonCache = new Map<string, string>();
  copiedJson = false;

  /** template 直接使用的日期格式化 helper */
  fmtDate = formatFhirDate;

  authMethodOptions = [
    { label: 'client_secret_basic', value: 'client_secret_basic' },
    { label: 'client_secret_post', value: 'client_secret_post' },
  ];

  /** Client Secret 欄位是否顯示明碼（眼睛切換，比照 ac0002 的 mask 慣例） */
  clientSecretMask: boolean = false;
  methods: { label: string; value: string }[] = CommonAPI.methods;

  resTime: string = '';
  startTimestamp: any;

  // 區塊一驗證鏈進行中的請求數（metadata / well-known 兩段可能接續發出，>0 時顯示 loading）
  private checkingCount = 0;

  destroy$ = new Subject<void>();

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: UntypedFormBuilder,
    private httpClient: HttpClient,
    private toolService: ToolService,
    private alert: AlertService,
    private serverService: ServerService,
    private ngxService: NgxUiLoaderService,
    private translate: TranslateService,
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      // 區塊一：Fhir base URL or well know
      sofProxyUrl: new UntypedFormControl(''),
      fhirBaseUrl: new UntypedFormControl(''),
      wellKnownUrl: new UntypedFormControl(''),
      tokenEndpoint: new UntypedFormControl(''),
      // 區塊二：Authorization（仿 api-test 測試區）
      authMethod: new UntypedFormControl('client_secret_basic'),
      clientId: new UntypedFormControl(''),
      clientSecret: new UntypedFormControl(''),
      method: new UntypedFormControl('POST'),
      testUrl: new UntypedFormControl(''),
      ContentType: new UntypedFormControl('application/x-www-form-urlencoded'),
      requestBody: new UntypedFormControl('formurl'),
      keyValueRequest: new UntypedFormControl(''),
      keyValueForm: new UntypedFormControl(''),
      bodyText: new UntypedFormControl(),
      resBody: new UntypedFormControl({ value: '', disabled: false }),
      resStatus: new UntypedFormControl({ value: '', disabled: false }),
      headerList: new UntypedFormControl({ value: '', disabled: false }),
      // Tab 2：Patient Dashboard（病人搜尋條件與關鍵字）
      patientSearchMode: new UntypedFormControl('name'),
      patientKeyword: new UntypedFormControl(''),
    });

    this.loadProxyList();

    // 下拉選擇 proxy 後自動帶入 FHIR Base URL（仍可手動修改）並立即驗證
    this.sofProxyUrl.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((url: string) => {
        if (url) {
          this.fhirBaseUrl.setValue(url);
          this.refreshFhirBaseUrl();
        }
      });

    // well-known 驗證鏈：查得 smart-configuration 後取出 token_endpoint
    this.wellKnownUrl.valueChanges
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((url: string) => {
          const u = (url || '').trim();
          if (!u) return of({ config: null as SmartConfiguration | null, empty: true });
          this.beginCheckLoading();
          return this.httpClient
            .get<SmartConfiguration>(u, { headers: this.bypassHeaders() })
            .pipe(
              map((config) => ({ config, empty: false })),
              catchError(() => of({ config: null as SmartConfiguration | null, empty: false })),
              // finalize 在完成或被 switchMap 取消時都會執行，確保 loading 一定會關閉
              finalize(() => this.endCheckLoading()),
            );
        }),
        takeUntil(this.destroy$),
      )
      .subscribe(({ config, empty }) => {
        this.smartConfig = config;
        this.wellKnownNotFound = !empty && !config;
        this.tokenEndpoint.setValue(config?.token_endpoint ?? '');
        // 取得 token_endpoint 後自動帶入測試 URL（仍可手動修改）
        if (config?.token_endpoint) {
          this.form.get('testUrl')!.setValue(config.token_endpoint);
        }
      });

    // 請求內文型態切換時自動調整 Content-Type（同 api-test 行為）
    this.form.get('requestBody')!.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe((res) => {
        switch (res) {
          case 'body':
          case 'none':
            this.form.get('ContentType')!.setValue('application/json');
            break;
          case 'form':
            this.form.get('ContentType')!.setValue('multipart/form-data');
            break;
          case 'formurl':
            this.form.get('ContentType')!.setValue('application/x-www-form-urlencoded');
            this.syncAuthBodyRows();
            break;
        }
      });

    // Auth Method 切換時：清除回應區內容，並清空 keyValueForm 後重新帶入 OAuth 自動列
    this.authMethod.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.clearResponse();
        this.syncAuthBodyRows(true);
      });

    // client_secret_post 時，表身 client_id / client_secret 列的值與上方欄位即時同步
    this.clientId.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.authMethod.value === 'client_secret_post') this.syncAuthBodyRows();
      });
    this.clientSecret.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        if (this.authMethod.value === 'client_secret_post') this.syncAuthBodyRows();
      });

    // 預設即為 client_secret_basic + x-www-form-urlencoded，初始就帶入 grant_type 列
    this.syncAuthBodyRows();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * 查詢 DPB0310 SMART on FHIR Proxy 清單，
   * 逐一展開每個 proxy 的 diversionList，組出
   * {origin}/smart-on-fhir/{sofProxyName}{diversion URL 的 path 部分}
   * 並去除重複結果作為下拉選項。
   */
  loadProxyList() {
    this.ngxService.start();
    this.serverService.querySmartOnFhirProxyList_ignore1298({}).subscribe({
      next: (res) => {
        const proxies = (res.RespBody?.content || []).filter(
          (p) => p.sofProxyStatus === 'Y' && p.sofProxyName,
        );
        const urls = new Set<string>();
        proxies.forEach((p) => {
          (p.diversionList || []).forEach((d) => {
            const path = SmartFhirService.extractFhirBasePath(d.sofProxyDiversionUrl);
            urls.add(
              `${environment.production?window.location.origin: environment.apiUrl}/smart-on-fhir/${p.sofProxyName}${path === '/' ? '' : path}`,
            );
          });
        });
        this.proxyUrlOptions = Array.from(urls);
        this.ngxService.stop();
      },
      error: () => this.ngxService.stop(),
    });
  }

  /**
   * 手動刷新 FHIR Base URL（由輸入框旁的刷新鈕、或選取 proxy 下拉時觸發）：
   * 以 HEAD {base}/metadata 確認站台是否存在，確認成功才組出 well-known URL，
   * 觸發後續的 well-known 驗證鏈取得 token_endpoint。
   * 註：HEAD 不回內文，故本段僅能判定「站台存在」，無法確認對方確實是 FHIR server
   *（改用 HEAD 是為了省下 CapabilityStatement 的龐大回應；
   * 是否為 FHIR server 交由後續 well-known 驗證鏈把關）。
   * 註：輸入時不自動驗證，避免每次 keydown 都打 metadata。
   */
  refreshFhirBaseUrl(): void {
    const base = this.currentBaseUrl();
    if (!base) {
      this.baseUrlNotFound = false;
      this.wellKnownUrl.setValue('');
      this.clearPatientDashboard();
      return;
    }
    this.beginCheckLoading();
    this.httpClient
      .head(`${base}/metadata`, { headers: this.bypassHeaders(), observe: 'response' })
      .pipe(
        map(() => true),
        // 405（不接受 HEAD）、401/403（需驗證）都代表端點確實存在，一律視為通過；
        // 404 與 status 0（CORS / DNS / 連線失敗）才視為不存在
        catchError((err: HttpErrorResponse) => of([401, 403, 405].includes(err.status))),
        // finalize 在完成或被取消時都會執行，確保 loading 一定會關閉
        finalize(() => this.endCheckLoading()),
        takeUntil(this.destroy$),
      )
      .subscribe((ok) => {
        this.baseUrlNotFound = !ok;
        this.wellKnownUrl.setValue(ok ? `${base}/.well-known/smart-configuration` : '');
        // 不論驗證成功與否，都先清掉 Patient Dashboard 既有內容，避免殘留前一個 base 查回的資料；
        // 清除時一併重設 lastPatientListBase，未停留在本分頁時也會在切回時以新 base 重查。
        this.clearPatientDashboard();
        // 驗證成功且正停留在 Patient Dashboard 時，立即以新的 base 重新查詢病人清單
        if (ok && this.activeTab === 'tab2') this.onSearchPatients();
      });
  }

  // ==================== Tab 2：Patient Dashboard ====================

  /** 切換分頁；切到 Patient Dashboard 時若清單尚未載入（或 base URL 已變更）則重新載入 */
  switchTab(tab: 'authorization' | 'tab2'): void {
    this.activeTab = tab;
    if (tab === 'tab2') {
      const base = this.currentBaseUrl();
      if (base && base !== this.lastPatientListBase) {
        this.lockPatientSearchCondition();
        this.loadPatientPage(0, false);
      }
    }
  }

  /** 搜尋按鈕 / Enter：以目前搜尋條件與關鍵字重新查詢病人清單 */
  onSearchPatients(): void {
    this.lockPatientSearchCondition();
    this.loadPatientPage(0, false);
  }

  /** 鎖定本次查詢用的條件與關鍵字，避免翻頁期間受表單變動影響 */
  private lockPatientSearchCondition(): void {
    this.patientQuery = (this.patientKeyword.value || '').trim();
    this.patientQueryMode = this.patientSearchMode.value as PatientSearchMode;
  }

  /**
   * 刷新按鈕：以區塊一「FHIR Base URL」輸入框當下的值重新查詢病人清單。
   * 停留在本分頁時修改 base URL 不會自動重載（重載僅由 switchTab 觸發），
   * 故提供手動刷新；查詢路徑一律取 currentBaseUrl()，與是否使用 proxy 下拉無關。
   */
  onRefreshPatients(): void {
    this.onSearchPatients();
  }

  /** 「載入更多」：往後翻一頁並附加到既有清單 */
  loadMorePatients(): void {
    if (this.hasMorePatients && !this.patientsLoadingMore) {
      this.loadPatientPage(this.patientOffset + this.patientPageSize, true);
    }
  }

  /**
   * 病人搜尋 URL：GET {base}/Patient?_sort=_id&_count=20&_offset={offset}&_format=json
   * 關鍵字為空 → 不附加搜尋參數（全域搜尋）；
   * 有關鍵字 → 依使用者選的條件（名稱 / 證件號）附加對應的 FHIR 搜尋參數。
   */
  private patientSearchUrl(base: string, offset: number): string {
    let url = `${base}/Patient?_sort=_id&_count=${this.patientPageSize}&_offset=${offset}&_format=json`;
    const kw = this.patientQuery;
    if (kw) {
      url += `&${PATIENT_SEARCH_PARAM[this.patientQueryMode]}=${encodeURIComponent(kw)}`;
    }
    return url;
  }

  private loadPatientPage(offset: number, append: boolean): void {
    const base = this.currentBaseUrl();
    if (!base) return;
    if (append) this.patientsLoadingMore = true;
    else this.ngxService.start();
    this.httpClient
      .get<Bundle<Patient>>(this.patientSearchUrl(base, offset), { headers: this.fhirQueryHeaders() })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (bundle) => {
          const list = (bundle.entry || [])
            .map((e) => e.resource)
            .filter((r): r is Patient => !!r && r.resourceType === 'Patient')
            .map(toPatientVM);
          this.patients = append ? [...this.patients, ...list] : list;
          this.patientOffset = offset;
          this.patientTotal = bundle.total ?? null;
          // 是否還有下一頁：優先看 bundle.total，無 total 時看 link 的 next
          this.hasMorePatients =
            this.patientTotal !== null
              ? offset + this.patientPageSize < this.patientTotal
              : !!SmartFhirService.nextLink(bundle);
          this.patientsFailed = false;
          this.patientsLoaded = true;
          this.lastPatientListBase = base;
          if (!append) {
            this.clearSelection();
            // 比照 digiCare：清單載入後自動選取第一位病人
            if (this.patients[0]) this.selectPatient(this.patients[0]);
          }
          this.finishPatientLoading(append);
        },
        error: () => {
          if (!append) {
            this.patients = [];
            this.clearSelection();
          }
          this.patientsFailed = true;
          this.patientsLoaded = true;
          this.hasMorePatients = false;
          this.finishPatientLoading(append);
        },
      });
  }

  private finishPatientLoading(append: boolean): void {
    if (append) this.patientsLoadingMore = false;
    else this.ngxService.stop();
  }

  /** 點選病人：載入該病人的所有 resource（$everything）並回到病歷摘要檢視 */
  selectPatient(p: PatientVM): void {
    if (this.selectedPatient?.id === p.id) return;
    this.clearSelection();
    this.selectedPatient = p;
    this.loadEverything(p.id);
  }

  /**
   * 取得指定 Patient 的 resource（分頁載入）：
   * GET {base}/Patient/{id}/$everything?_count={pageSize}&_offset=0&_sort=_id&_format=json
   * 首次載入第一頁，後續換頁由 paginator 依總筆數 / 每頁筆數觸發。
   */
  private loadEverything(patientId: string): void {
    const base = this.currentBaseUrl();
    if (!base || !patientId) return;
    this.everythingFirst = 0;
    this.fetchEverythingPage(base, patientId, 0);
  }

  /** Paginator 換頁：載入該頁（offset = event.first）的 $everything 資料（取代目前頁） */
  onEverythingPageChange(event: PaginatorState): void {
    const offset = event.first ?? 0;
    // 換到同一頁（含 paginator 因輸入綁定變動而回拋）時不重複請求
    if (!this.selectedPatient || offset === this.everythingFirst) return;
    const base = this.currentBaseUrl();
    if (!base) return;
    this.everythingFirst = offset;
    this.fetchEverythingPage(base, this.selectedPatient.id, offset);
  }

  /**
   * 抓取指定頁 $everything（自 offset 起、pageSize 筆），成功後取代目前頁並重建檢視。
   */
  private fetchEverythingPage(base: string, patientId: string, offset: number): void {
    const url =
      `${base}/Patient/${patientId}/$everything` +
      `?_count=${this.everythingPageSize}&_offset=${offset}&_sort=_id&_format=json`;
    this.ngxService.start();
    this.httpClient
      .get<Bundle>(url, { headers: this.fhirQueryHeaders() })
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (bundle) => {
          // 使用者已切換到別的病人時，捨棄過期回應
          if (this.selectedPatient?.id !== patientId) return;
          const entries = (bundle.entry || [])
            .map((e) => e.resource)
            .filter((r): r is FhirResource => !!r);
          // paginator 依總筆數 / 每頁筆數計算頁數；部分伺服器僅首頁回傳 total，缺漏時沿用前值
          this.everythingTotal = bundle.total ?? this.everythingTotal;
          this.everythingFailed = false;
          this.bindEverything(patientId, entries);
          this.ngxService.stop();
        },
        error: () => {
          if (this.selectedPatient?.id !== patientId) return;
          // 失敗時清空目前頁並以紅字提示（everythingFailed）
          this.everythingFailed = true;
          this.everythingTotal = null;
          this.bindEverything(patientId, []);
          this.ngxService.stop();
        },
      });
  }

  /** 目前頁 $everything 載入後：組出參照樹、就診時間軸與完整 JSON */
  private bindEverything(patientId: string, resources: FhirResource[]): void {
    this.everythingEntries = resources;
    this.everythingLoaded = true;
    const { tree, byKey, typeCount } = buildResourceTree(patientId, resources);
    this.tree = tree;
    this.byKey = byKey;
    this.resourceTypeCount = typeCount;
    // 以 $everything 取回的完整 Patient 資源補齊摘要欄位（清單查詢可能缺 telecom / address）
    const fullPatient = byKey.get(`Patient/${patientId}`);
    if (fullPatient) this.selectedPatient = toPatientVM(fullPatient as Patient);
    // 就診時間軸：依開始時間新到舊
    this.encounters = resources
      .filter((r) => r.resourceType === 'Encounter')
      .map(toEncounterVM)
      .sort((a, b) => (b.start || '').localeCompare(a.start || ''));
    // 過篩資料樹：預設展開 Patient 根節點
    this.revealed = new Set(tree[0] ? [`${tree[0].type}/${tree[0].id}`] : []);
    this.refreshVisibleRows();
    this.everythingJson = JSON.stringify(
      {
        resourceType: 'Bundle',
        type: 'collection',
        total: resources.length,
        entry: resources.map((r) => ({ resource: r })),
      },
      null,
      2,
    );
  }

  /**
   * 清空 Patient Dashboard 的所有資料（病人清單 + 已選病人的 $everything），
   * 回到「尚未載入」的初始狀態。base URL 取不到資料時呼叫，避免殘留前一個 base 的內容。
   */
  private clearPatientDashboard(): void {
    this.patients = [];
    this.patientTotal = null;
    this.patientsFailed = false;
    this.patientsLoaded = false;
    this.patientsLoadingMore = false;
    this.hasMorePatients = false;
    this.patientOffset = 0;
    // 一併清掉上次查詢的 base，使下次切回本分頁時必定重新查詢
    this.lastPatientListBase = '';
    this.clearSelection();
  }

  /** 清除已選病人與右側檢視狀態 */
  private clearSelection(): void {
    this.selectedPatient = null;
    this.viewMode = 'summary';
    this.everythingFailed = false;
    this.everythingLoaded = false;
    this.everythingEntries = [];
    this.everythingFirst = 0;
    this.everythingTotal = null;
    this.everythingJson = '';
    this.resourceTypeCount = 0;
    this.encounters = [];
    this.tree = [];
    this.byKey = new Map();
    this.revealed = new Set();
    this.visibleRows = [];
    this.expandedCards = new Set();
    this.cardJsonCache = new Map();
    this.copiedJson = false;
  }

  /** JSON.stringify 成本較高，僅在展開狀態變動時重算可見列，不放在 getter 被變更偵測反覆執行 */
  private refreshVisibleRows(): void {
    this.visibleRows = flattenVisible(this.tree, this.revealed, (key) => {
      const res = this.byKey.get(key);
      return res ? summarize(res, (g, c) => this.codeLabel(g, c)) : '';
    });
  }

  /** 過篩資料樹的 ＋/− 展開切換（右側資源檢視同步顯示） */
  toggleReveal(row: VisibleRow): void {
    if (this.revealed.has(row.key)) this.revealed.delete(row.key);
    else this.revealed.add(row.key);
    this.refreshVisibleRows();
  }

  /** 資源檢視卡片展開 / 收合；首次展開時快取該資源的 JSON 字串 */
  toggleCard(row: VisibleRow): void {
    if (this.expandedCards.has(row.key)) {
      this.expandedCards.delete(row.key);
      return;
    }
    this.expandedCards.add(row.key);
    if (!this.cardJsonCache.has(row.key)) {
      const res = this.byKey.get(row.key);
      this.cardJsonCache.set(row.key, res ? JSON.stringify(res, null, 2) : '');
    }
  }

  cardJson(key: string): string {
    return this.cardJsonCache.get(key) ?? '';
  }

  /** 就診卡片展開 / 收合；首次展開時從 $everything 結果組出明細 */
  toggleEncounter(vm: EncounterVM): void {
    vm.expanded = !vm.expanded;
    if (vm.expanded && !vm.detailLoaded) {
      fillEncounterDetail(vm, this.byKey, this.everythingEntries);
      vm.detailLoaded = true;
    }
  }

  /** 複製完整 JSON 到剪貼簿，按鈕短暫顯示「已複製」 */
  copyJson(): void {
    navigator.clipboard.writeText(this.everythingJson).then(() => {
      this.copiedJson = true;
      setTimeout(() => (this.copiedJson = false), 1500);
    });
  }

  /**
   * FHIR code → 多語系顯示文字（fhir_viewer.{group}.{code}），
   * 語系檔無對應 key 時顯示原始 code
   */
  codeLabel(group: string, code: string): string {
    if (!code) return '';
    const key = `fhir_viewer.${group}.${code}`;
    const v = this.translate.instant(key);
    return v === key ? code : v;
  }

  encClassColor(code: string): string {
    return ENC_CLASS_COLORS[code] ?? ENC_CLASS_DEFAULT_COLOR;
  }

  medStatusClass(status: string): string {
    return Lb0018Component.MED_STATUS_CLS[status] ?? 'pd-pill--gray';
  }

  get patientAge(): number | null {
    return this.selectedPatient ? calcAge(this.selectedPatient.birthDate) : null;
  }

  /** 區塊一輸入的 FHIR Base URL（去除尾端斜線） */
  private currentBaseUrl(): string {
    return this.trimTrailingSlash((this.fhirBaseUrl.value || '').trim());
  }

  get hasFhirBase(): boolean {
    return !!this.currentBaseUrl();
  }

  /**
   * 區塊一驗證與 FHIR 查詢請求的共用 headers：
   * - digiRunner：讓 TokenInterceptor 跳過回應處理，404 等錯誤不彈全域提示（比照 api-test）
   * - Authorization + X-Smart-Launcher：比照 lb0016，讓 gateway 的 /smart-on-fhir/* 通過入站驗證
   */
  private bypassHeaders(): HttpHeaders {
    return new HttpHeaders({
      digiRunner: 'Test Process',
      'X-Smart-Launcher': 'true',
      Accept: 'application/fhir+json, application/json',
    });
  }

  /**
   * Patient Dashboard 的 FHIR 查詢（病人清單 / $everything）專用表頭：
   * 在區塊一驗證共用的 bypassHeaders 之上，若 Authorization 分頁已取得
   * access_token，額外帶入 Bearer token。驗證階段（metadata / well-known）
   * 不使用本方法，故不會帶上查詢用的 token。
   */
  private fhirQueryHeaders(): HttpHeaders {
    let headers = this.bypassHeaders();
    if (this.accessToken) {
      headers = headers.append('Authorization', `${this.tokenType} ${this.accessToken}`);
    }
    return headers;
  }

  /**
   * 從 Authorization 分頁的回應嘗試擷取 access_token 記錄下來，
   * 供 Tab 2 Patient Dashboard 的 FHIR 查詢帶入 Bearer token。
   * 僅在 2xx 且回應內文可解析出 access_token 時更新；其餘情況（非 2xx、
   * 回應非 JSON、無 access_token）皆保留既有的 token，不覆寫。
   */
  private captureAccessToken(bodyText: string, status: number): void {
    if (status < 200 || status >= 300 || !bodyText) return;
    try {
      const json = JSON.parse(bodyText);
      const token = json?.access_token;
      if (typeof token === 'string' && token) {
        this.accessToken = token;
        const type = json?.token_type;
        // token_type 依 OAuth 慣例大小寫不敏感，統一顯示為 Bearer；非 bearer 型別則沿用原值
        this.tokenType =
          typeof type === 'string' && type.toLowerCase() !== 'bearer' && type.trim() !== ''
            ? type
            : 'Bearer';
      }
    } catch {
      // 回應非 JSON（或不含 access_token）時不更新既有 token
    }
  }

  /** 清除已記錄的 access_token（Patient Dashboard 查詢將不再帶入 Bearer） */
  clearAccessToken(): void {
    this.accessToken = null;
    this.tokenType = 'Bearer';
  }

  private trimTrailingSlash(url: string): string {
    return url.endsWith('/') ? url.slice(0, -1) : url;
  }

  /**
   * 區塊一驗證鏈的 loading 控制：以計數器避免 metadata / well-known
   * 兩段請求重疊時重複 start/stop（ngxService 同一 task 多次 stop 會提早關閉）。
   */
  private beginCheckLoading(): void {
    if (this.checkingCount++ === 0) this.ngxService.start();
  }

  private endCheckLoading(): void {
    if (this.checkingCount > 0 && --this.checkingCount === 0) this.ngxService.stop();
  }

  /** 請求表頭 / 內文 key-value 區塊的「增加列」按鈕（同 api-test） */
  public add(componentName: string): void {
    componentName === 'request' ? this.kv_request!.add() : this.kv_form.add();
  }

  /** 送出測試請求（仿 api-test 的 send()，auth 改為 client_secret_basic / client_secret_post） */
  async send(): Promise<void> {
    const codes = [
      'dialog.warn',
      'client_id_required',
      'fhir_viewer.client_secret_required',
      'fhir_viewer.url_required',
      'header_key_required',
    ];
    const dict = await this.toolService.getDict(codes);

    this.clearResponse();

    const clientId = this.clientId.value;
    const clientSecret = this.clientSecret.value;
    if (!clientId || clientId === '') {
      this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
      return;
    }
    if (!clientSecret || clientSecret === '') {
      this.alert.ok(dict['dialog.warn'], dict['fhir_viewer.client_secret_required']);
      return;
    }
    if (!this.form.get('testUrl')!.value || this.form.get('testUrl')!.value.trim() === '') {
      this.alert.ok(dict['dialog.warn'], dict['fhir_viewer.url_required']);
      return;
    }

    let headerList: HttpHeader[] = new Array<HttpHeader>();
    headerList = headerList.concat(this.keyvalueConvetToList(this.form.get('keyValueRequest')!.value));

    let isVaild: boolean = true;
    headerList.forEach(function (value) {
      if (value != undefined) {
        if (value.name.trim() == '' && value.value.trim() != '') {
          isVaild = false;
        }
      }
    });
    if (isVaild != true) {
      this.alert.ok(dict['dialog.warn'], dict['header_key_required']);
      return;
    }

    const path = this.form.get('testUrl')!.value;
    const body = this.reqBodyHandler();

    // 自動帶入請求表頭：digiRunner 標記 + client_secret_basic 時帶 Basic Authorization
    let header = new HttpHeaders({
      digiRunner: 'Test Process', // api測試區標記，在token intercepter內辨識流程用
    });
    if (this.authMethod.value === 'client_secret_basic') {
      header = header.append('Authorization', this.basicAuthorization);
    }
    const contentType: string = this.form.get('ContentType')!.value;
    // content-Type（若是 multipart/form-data 不用加）
    if (contentType != '' && contentType.toLowerCase() != 'multipart/form-data'.toLowerCase()) {
      header = header.append('Content-Type', contentType);
    }
    headerList.forEach(function (value) {
      if (value != undefined) {
        if (value.name != '' && value.value != '') {
          header = header.append(value.name, value.value);
        }
      }
    });

    // 每次實際打 API 前先清除既有 access_token；若本次成功取得新 token，會於回應時重新記錄
    this.clearAccessToken();

    this.startTimestamp = new Date().getTime();
    this.ngxService.start();
    this.httpClientHandle(path, header, body);
  }

  /**
   * 依 Auth Method 同步請求內文（keyValueForm）的 OAuth 自動列（RFC 6749）：
   * - 兩種模式都帶 grant_type=client_credentials
   * - client_secret_post 額外帶 client_id / client_secret（值與上方欄位即時同步）
   * - 切回 client_secret_basic 時移除 client_id / client_secret 列（憑證不殘留在 body）
   * 自動列每次重建（先過濾同名 key 再帶入）；clearUserRows 為 true 時（切換
   * Auth Method）連使用者自訂列一併清除，重新只帶入自動列。
   */
  private syncAuthBodyRows(clearUserRows: boolean = false): void {
    if (this.form.get('requestBody')!.value !== 'formurl') return;
    const ctrl = this.form.get('keyValueForm')!;
    const rows: { key: string; value: string; selected?: boolean }[] = Array.isArray(ctrl.value)
      ? ctrl.value
      : [];
    const autoKeys = ['grant_type', 'client_id', 'client_secret'];
    const userRows = clearUserRows ? [] : rows.filter((r) => r && !autoKeys.includes(r.key));
    const autoRows: { key: string; value: string; selected: boolean }[] = [
      { key: 'grant_type', value: 'client_credentials', selected: true },
    ];
    if (this.authMethod.value === 'client_secret_post') {
      autoRows.push(
        { key: 'client_id', value: this.clientId.value || '', selected: true },
        { key: 'client_secret',  value: this.toolService.Base64Encoder(this.clientSecret.value || ''), selected: true },
      );
    }
    // writeValue 收到陣列時會先清空既有列再帶入，不會殘留空白列
    ctrl.setValue([...autoRows, ...userRows]);
  }

  /** 清除「回應」區塊內容（狀態 / 表頭 / 內文 / 耗時） */
  private clearResponse(): void {
    this.form.get('resBody')!.setValue('');
    this.form.get('resStatus')!.setValue('');
    this.form.get('headerList')!.setValue('');
    this.resTime = '';
  }

  /**
   * 組出請求內文（同 api-test 的 reqBodyHandler）。
   * x-www-form-urlencoded 時，兩種 auth method 的 OAuth 參數
   * （grant_type，post 模式另含 client_id / client_secret）都由 keyValueForm 列組出
   * （見 syncAuthBodyRows()）；client_secret_basic 的憑證則走請求表頭的
   * Basic Authorization（見 send()）。
   */
  private reqBodyHandler(): any {
    let body: any;
    switch (this.form.get('requestBody')!.value) {
      case 'body':
        body = this.form.get('bodyText')!.value;
        break;
      case 'form': {
        let paramList: FormParam[] = new Array<FormParam>();
        paramList = paramList.concat(this.keyvalueConvetToList(this.form.get('keyValueForm')!.value));
        body = new FormData();
        for (let i = 0; i < paramList.length; i++) {
          body.append(paramList[i].name, paramList[i].value);
        }
        break;
      }
      case 'formurl': {
        const params = new URLSearchParams();
        // OAuth 參數（grant_type / client_id / client_secret）皆由 keyValueForm 列帶入（syncAuthBodyRows()）
        let formurlList: FormParam[] = new Array<FormParam>();
        formurlList = formurlList.concat(this.keyvalueConvetToList(this.form.get('keyValueForm')!.value));
        formurlList.forEach((p) => params.set(p.name, p.value));
        body = params.toString();
        break;
      }
      case 'none':
      default:
        body = null;
        break;
    }
    return body;
  }

  /** 依 method 發出實際 HTTP 請求（同 api-test 的 httpClientHandle，不含 mock / stream） */
  private httpClientHandle(path: string, header: HttpHeaders, body?: any) {
    const options = { headers: header, observe: 'response' as const, responseType: 'text' as const };
    const handler = {
      next: (v: any) => this.bindResResult(v),
      error: (e: any) => this.bindErrResult(e),
      complete: () => this.ngxService.stop(),
    };

    switch (this.form.get('method')!.value.toUpperCase()) {
      case 'GET':
        this.httpClient.get(`${path}`, options).subscribe(handler);
        break;
      case 'POST':
        this.httpClient.post(`${path}`, body, options).subscribe(handler);
        break;
      case 'PUT':
        this.httpClient.put(`${path}`, body, options).subscribe(handler);
        break;
      case 'DELETE':
        this.httpClient.delete(`${path}`, { ...options, body: body }).subscribe(handler);
        break;
      case 'PATCH':
        this.httpClient.patch(`${path}`, body, options).subscribe(handler);
        break;
      default:
        this.ngxService.stop();
        break;
    }
  }

  private bindResResult(data: { headers: HttpHeaders; body: any; status: number; [key: string]: any }): void {
    if (!data || !data.headers || typeof data.status !== 'number') {
      console.error('Invalid response data');
      this.ngxService.stop();
      return;
    }
    this.calResponseTime();

    const mainHeaders: Record<string, string | null> = {};
    data.headers.keys().forEach((key) => {
      const value = data.headers.get(key);
      if (value !== null) {
        mainHeaders[key] = value;
      }
    });

    this.form.get('resBody')!.setValue(data.body);
    this.form.get('resStatus')!.setValue(data.status);
    this.form.get('headerList')!.setValue(JSON.stringify(mainHeaders));

    // 成功回應時嘗試擷取 access_token，供 Patient Dashboard 的 FHIR 查詢帶入
    this.captureAccessToken(data.body, data.status);

    this.ngxService.stop();
  }

  bindErrResult(data: { headers: HttpHeaders; body: any; status: number; error?: any }): void {
    if (!data || !data.headers || typeof data.status !== 'number') {
      console.error('Invalid response data');
      this.ngxService.stop();
      return;
    }
    this.calResponseTime();

    const mainHeaders: Record<string, string | null> = {};
    data.headers.keys().forEach((key) => {
      const value = data.headers.get(key);
      if (value !== null) {
        mainHeaders[key] = value;
      }
    });

    const bodymsg = typeof data.error == 'object' ? JSON.stringify(data.error) : data.error;
    this.form.get('resBody')!.setValue(bodymsg);
    this.form.get('resStatus')!.setValue(data.status);
    this.form.get('headerList')!.setValue(JSON.stringify(mainHeaders));

    this.ngxService.stop();
  }

  keyvalueConvetToList(keyvalues: { key: string; value: string | File; selected?: boolean }[]) {
    let formatResult: HttpHeader[] = [];
    if (keyvalues && keyvalues.length) {
      formatResult = keyvalues
        .filter((item) => item.selected)
        .flatMap((keyvalue) => {
          if (Array.isArray(keyvalue.value)) {
            return keyvalue.value.map((v) => ({ name: keyvalue.key, value: v } as HttpHeader));
          }
          return { name: keyvalue.key, value: keyvalue.value } as HttpHeader;
        });
    }
    return formatResult;
  }

  private calResponseTime(): void {
    const endTimestamp: number = new Date().getTime();
    const responseTimes = endTimestamp - this.startTimestamp;
    this.resTime = responseTimes + 'ms';
  }

  /** client_secret_basic 自動帶入的 Authorization 表頭值：'Basic ' + base64(client_id:client_secret) */
  get basicAuthorization(): string {
    const account = `${this.clientId.value || ''}:${this.toolService.Base64Encoder(this.clientSecret.value || '')}`;
    return `Basic ${this.toolService.Base64Encoder(account)}`;
  }

  // ===== 表單 getter =====
  public get sofProxyUrl() {
    return this.form.get('sofProxyUrl') as UntypedFormControl;
  }
  public get fhirBaseUrl() {
    return this.form.get('fhirBaseUrl') as UntypedFormControl;
  }
  public get wellKnownUrl() {
    return this.form.get('wellKnownUrl') as UntypedFormControl;
  }
  public get tokenEndpoint() {
    return this.form.get('tokenEndpoint') as UntypedFormControl;
  }
  public get authMethod() {
    return this.form.get('authMethod') as UntypedFormControl;
  }
  public get clientId() {
    return this.form.get('clientId') as UntypedFormControl;
  }
  public get clientSecret() {
    return this.form.get('clientSecret') as UntypedFormControl;
  }
  public get patientSearchMode() {
    return this.form.get('patientSearchMode') as UntypedFormControl;
  }
  public get patientKeyword() {
    return this.form.get('patientKeyword') as UntypedFormControl;
  }
}

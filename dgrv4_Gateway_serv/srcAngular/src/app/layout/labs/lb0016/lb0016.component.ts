import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { MessageService } from 'primeng/api';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { SmartFhirService, PatientDisplay, PractitionerDisplay, EncounterDisplay } from './smart-fhir.service';
import { Bundle, Patient, Practitioner, Encounter } from './smart-fhir.types';
import { SmartClientDto } from 'src/app/models/api/ServerService/dpb0330.interface';
import { SmartClientUpdateItem } from 'src/app/models/api/ServerService/dpb0332.interface';
import { DPB0331ReqBody } from 'src/app/models/api/ServerService/dpb0331.interface';
import { SmartOnFhirProxyDto } from 'src/app/models/api/ServerService/dpb0310.interface';
import { SMART_CLIENT_TYPES, SMART_LAUNCH_MODES, SMART_AUTO_APPROVE_OPTIONS, SMART_IDP_TYPES } from 'src/app/models/api/ServerService/smart-client.constants';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { AA0202List } from 'src/app/models/api/ClientService/aa0202.interface';

export type LaunchType =
  | 'provider_ehr'
  | 'provider_standalone'
  | 'patient_standalone'
  | 'patient_portal'
  | 'backend_services';

@Component({
  selector: 'app-lb0016',
  templateUrl: './lb0016.component.html',
  styleUrls: ['./lb0016.component.scss'],
  standalone: false,
  providers: [MessageService],
})
export class Lb0016Component extends BaseComponent implements OnInit {

  activeTab: 'launch' | 'client' = 'launch';

  launchType: LaunchType = 'provider_ehr';

  launchTypeOptions: { value: LaunchType; labelKey: string }[] = [
    { value: 'provider_ehr',        labelKey: 'smart_launcher.launch_type_provider_ehr' },
    { value: 'provider_standalone', labelKey: 'smart_launcher.launch_type_provider_standalone' },
    { value: 'patient_standalone',  labelKey: 'smart_launcher.launch_type_patient_standalone' },
    { value: 'patient_portal',      labelKey: 'smart_launcher.launch_type_patient_portal' },
    { value: 'backend_services',    labelKey: 'smart_launcher.launch_type_backend_services' },
  ];

  launchTypeDescription: string = '';

  // 值為 i18n key，template 以 | translate 顯示
  private launchTypeDescriptions: Record<LaunchType, string> = {
    provider_ehr:        'smart_launcher.launch_type_desc_provider_ehr',
    provider_standalone: 'smart_launcher.launch_type_desc_provider_standalone',
    patient_standalone:  'smart_launcher.launch_type_desc_patient_standalone',
    patient_portal:      'smart_launcher.launch_type_desc_patient_portal',
    backend_services:    'smart_launcher.launch_type_desc_backend_services',
  };

  showClientSecret: boolean = false;
  showJwks: boolean = false;
  showPkce: boolean = true;
  showProvider: boolean = true;

  proxyName: string = '';
  proxyNameOptions: string[] = [];
  /** proxyName → fhirBasePath（從 diversionList[0].sofProxyDiversionUrl 解析） */
  private proxyBasePathMap: Record<string, string> = {};
  /** 當前 proxy 的 fhirBasePath，"/" 表示後端 FHIR server 沒有 base path */
  selectedFhirBasePath: string = '/';

  clientType: string = 'public';
  clientId: string = '';
  clientSecret: string = '';
  jwksUrl: string = '';
  jwksJson: string = '';
  pkceMode: string = 'auto';

  pkceModeOptions: { value: string; labelKey: string }[] = [
    { value: 'none',   labelKey: 'smart_launcher.pkce_none' },
    { value: 'auto',   labelKey: 'smart_launcher.pkce_auto' },
    { value: 'always', labelKey: 'smart_launcher.pkce_always' },
  ];

  // picker 搜尋模式（name / id）下拉選項，供 p-select 使用
  searchModeOptions: { value: 'name' | 'id'; labelKey: string }[] = [
    { value: 'name', labelKey: 'smart_launcher.col_name' },
    { value: 'id',   labelKey: 'smart_launcher.col_id' },
  ];

  // encounter 選取模式下拉選項
  encounterModeOptions: { value: string; labelKey: string }[] = [
    { value: 'AUTO',   labelKey: 'smart_launcher.encounter_auto' },
    { value: 'MANUAL', labelKey: 'smart_launcher.encounter_manual' },
  ];

  patientId: string = '';
  encounterId: string = '';
  encounterMode: string = 'AUTO';
  encounterLoading: boolean = false;
  encounterError: string = '';
  encounterResults: EncounterDisplay[] = [];
  /** 逗號分隔的 provider ID 清單。0 個→流程中全域選；1 個→直接帶入；多個→流程中限定候選挑選 */
  providerIds: string = '';
  /** 選中的 provider ID 集合（多選 picker 用） */
  selectedProviderIdSet: Set<string> = new Set<string>();

  simEhr: boolean = false;
  skipLogin: boolean = false;
  skipAuth: boolean = false;
  simError: string = '';

  appLaunchUrl: string = '';
  redirectUri: string = '';

  scopeText: string = 'openid profile launch patient/*.cruds';

  scopeChips: { value: string; active: boolean }[] = [
    { value: 'openid',            active: true  },
    { value: 'profile',           active: true  },
    { value: 'fhirUser',          active: false },
    { value: 'launch',            active: true  },
    { value: 'launch/patient',    active: false },
    { value: 'launch/encounter',  active: false },
    { value: 'patient/*.cruds',   active: true  },
    { value: 'user/*.cruds',      active: false },
    { value: 'system/*.cruds',    active: false },
    { value: 'offline_access',    active: false },
    { value: 'online_access',     active: false },
  ];

  copySuccess: boolean = false;

  // ── Client Selector (下拉列 OAuth client，AA0202) ──
  oauthClients: AA0202List[] = [];
  selectedRegisteredClient: string = '';

  // ── SMART Config Editor state (復用既有 client 欄位編輯 dgr_smart_client) ──
  smartCfgLoading = false;
  smartCfgExists = false;         // 該 client 已有 dgr_smart_client → update；否則 create
  smartCfgVersion: number | null = null;
  // 既有 client 表單沒有、dgr_smart_client 才有的補充欄位
  smartIdpType = '';
  smartIdpClientId = '';
  smartLaunchMode = 'standalone';
  smartAutoApprove = 'N';
  smartAuthMethod = '';
  readonly cfgLaunchModes: string[] = [...SMART_LAUNCH_MODES];
  readonly cfgAutoApproveOptions: string[] = [...SMART_AUTO_APPROVE_OPTIONS];
  readonly cfgIdpTypes: string[] = [...SMART_IDP_TYPES];

  // ── Patient Picker ──
  showPatientPicker: boolean = false;
  patientSearchMode: 'name' | 'id' = 'name';
  patientSearchKeyword: string = '';
  patientSearching: boolean = false;
  patientSearchError: string = '';
  patientResults: PatientDisplay[] = [];
  patientPage: number = 1;            // 當前頁（1-based，前端自行計數）
  patientNextUrl: string | null = null;
  patientPrevUrl: string | null = null;
  readonly patientPageSize: number = 20;
  /** 多選 Patient ID 集合（picker 用） */
  selectedPatientIdSet: Set<string> = new Set<string>();

  // ── Provider Picker ──
  showProviderPicker: boolean = false;
  providerSearchMode: 'name' | 'id' = 'name';
  providerSearchKeyword: string = '';
  providerSearching: boolean = false;
  providerSearchError: string = '';
  providerResults: PractitionerDisplay[] = [];
  providerPage: number = 1;
  providerNextUrl: string | null = null;
  providerPrevUrl: string | null = null;
  readonly providerPageSize: number = 20;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private router: Router,
    private translate: TranslateService,
    private serverService: ServerService,
    private toolService: ToolService,
    private fhirService: SmartFhirService,
    private messageService: MessageService,
    private clientService: ClientService,
  ) {
    super(route, tr);
  }

  ngOnInit(): void {
    this.onLaunchTypeChange();
    this.loadProxyList();
    this.loadOauthClients();
  }

  private loadProxyList(): void {
    this.serverService.querySmartOnFhirProxyList_ignore1298({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const proxies: SmartOnFhirProxyDto[] = (res.RespBody.content || [])
          .filter(p => p.sofProxyStatus === 'Y' && p.sofProxyName);
        this.proxyNameOptions = proxies.map(p => p.sofProxyName!);
        this.proxyBasePathMap = {};
        proxies.forEach(p => {
          const url = p.diversionList?.[0]?.sofProxyDiversionUrl;
          this.proxyBasePathMap[p.sofProxyName!] = SmartFhirService.extractFhirBasePath(url);
        });
        if (this.proxyNameOptions.length > 0 && !this.proxyName) {
          this.proxyName = this.proxyNameOptions[0];
        }
        this.updateSelectedBasePath();
      }
    });
  }

  onProxyNameChange(): void {
    this.updateSelectedBasePath();
  }

  private updateSelectedBasePath(): void {
    this.selectedFhirBasePath = this.proxyBasePathMap[this.proxyName] || '/';
  }

  private loadOauthClients(): void {
    // encodeStatus：'-1'=全部，編碼方式對齊 AC0202（Base64(Bcrypt(value)) + ',' + queryIndex(2)）
    const encodeStatus = this.toolService.Base64Encoder(this.toolService.BcryptEncoder('-1')) + ',2';
    this.clientService.queryClientList_ignore1298({ keyword: '', groupID: '', encodeStatus }).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.oauthClients = res.RespBody.clientInfoList || [];
      }
    });
  }

  /** 下拉選一個 OAuth client → 帶入 clientId 並載入其 dgr_smart_client 設定到既有欄位。 */
  onRegisteredClientChange(): void {
    const cid = this.selectedRegisteredClient;
    if (!cid) {
      this.smartCfgExists = false;
      this.smartCfgVersion = null;
      return;
    }
    this.clientId = cid;
    this.loadSmartConfig(cid);
  }

  // ==================== SMART Config Editor ====================

  /**
   * 載入選定 client 的 dgr_smart_client 設定供編輯。
   * 複用 DPB0330 查詢，取得最新 version 作樂觀鎖。
   */
  loadSmartConfig(clientId: string): void {
    if (!clientId) return;
    this.smartCfgLoading = true;
    this.smartCfgExists = false;
    this.serverService.querySmartClientList({ keyword: clientId, pageNum: 0, pageSize: 1 }).subscribe({
      next: (res) => {
        this.smartCfgLoading = false;
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const match = (res.RespBody.content || []).find(c => c.clientId === clientId);
          if (match) {
            this.applySmartCfg(match);
            this.smartCfgExists = true;
          } else {
            this.resetSmartCfgFields();
            this.smartCfgExists = false;
          }
        }
      },
      error: () => {
        this.smartCfgLoading = false;
        this.smartCfgExists = false;
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('smart_launcher.cfg_load_fail'),
        });
      },
    });
  }

  /** 將查回的 dgr_smart_client 套入既有 client 欄位 + 補充欄位。列表不回傳 JWKS 原文，故留空。 */
  private applySmartCfg(dto: SmartClientDto): void {
    this.clientType = dto.clientType || 'public';
    this.onClientTypeChange();
    this.scopeText = this.joinList(dto.allowedScopes, ' ');
    this.onScopeTextChange();
    this.redirectUri = this.joinList(dto.redirectUris, ',');
    this.jwksUrl = dto.jwksUri || '';
    this.jwksJson = '';
    this.smartIdpType = dto.idpType || '';
    this.smartIdpClientId = dto.idpClientId || '';
    this.smartLaunchMode = dto.launchMode || 'standalone';
    this.smartAutoApprove = dto.autoApprove || 'N';
    this.smartAuthMethod = dto.tokenEndpointAuthMethod || '';
    this.smartCfgVersion = dto.version != null ? Number(dto.version) : null;
  }

  /** 後端查詢的 allowedScopes / redirectUris 可能是陣列或字串，統一轉為分隔字串。 */
  private joinList(v: unknown, sep: string): string {
    if (Array.isArray(v)) return v.join(sep);
    if (typeof v === 'string') return sep === ',' ? v.replace(/\n/g, ',') : v;
    return '';
  }

  /** 查無設定時清空補充欄位（既有 client 欄位保留使用者目前值供新增）。 */
  private resetSmartCfgFields(): void {
    this.smartIdpType = '';
    this.smartIdpClientId = '';
    this.smartLaunchMode = 'standalone';
    this.smartAutoApprove = 'N';
    this.smartAuthMethod = '';
    this.smartCfgVersion = null;
  }

  /** 該 clientType 允許的 token endpoint 驗證方式（供 auth method 下拉）。 */
  cfgAuthMethods(): string[] {
    const t = SMART_CLIENT_TYPES.find(x => x.key === this.clientType);
    return t ? t.allowedAuthMethods : [];
  }

  /**
   * 儲存 dgr_smart_client：既有 → DPB0332 update；查無 → DPB0331 create。
   * 從既有 client 欄位 + 補充欄位組裝；redirect 由逗號分隔轉回換行（後端格式）。
   * @param onSuccess 存檔成功後的後續動作（Launch App：存完再啟動）
   */
  saveSmartConfig(onSuccess?: () => void): void {
    const clientId = this.clientId;
    if (!clientId) {
      this.messageService.add({
        severity: 'warn',
        summary: this.translate.instant('smart_launcher.cfg_need_client'),
      });
      return;
    }
    // allowedScopes / redirectUris 後端為 List<String>，需送陣列
    const scopeArr = (this.scopeText || '').trim().split(/\s+/).filter(s => s);
    const redirectArr = (this.redirectUri || '').split(',').map(s => s.trim()).filter(s => s);

    if (this.smartCfgExists && this.smartCfgVersion != null) {
      const item: SmartClientUpdateItem = {
        clientId,
        version: this.smartCfgVersion,
        clientType: this.clientType || null,
        idpType: this.smartIdpType || null,
        allowedScopes: scopeArr.length ? scopeArr : null,
        redirectUris: redirectArr.length ? redirectArr : null,
        launchMode: this.smartLaunchMode || null,
        autoApprove: this.smartAutoApprove || null,
        tokenEndpointAuthMethod: this.smartAuthMethod || null,
        jwksUri: this.jwksUrl || null,
        jwks: this.jwksJson || null,
      };
      this.serverService.batchUpdateSmartClient({ updateList: [item] }).subscribe({
        next: (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.afterSaveSmartConfig(clientId);
            onSuccess?.();
          }
        },
        error: () => this.smartConfigSaveError(),
      });
    } else {
      const body: DPB0331ReqBody = {
        clientId,
        clientType: this.clientType || undefined,
        idpType: this.smartIdpType || undefined,
        idpClientId: this.smartIdpClientId || undefined,
        allowedScopes: scopeArr.length ? scopeArr : undefined,
        redirectUris: redirectArr.length ? redirectArr : undefined,
        launchMode: this.smartLaunchMode || undefined,
        autoApprove: this.smartAutoApprove || undefined,
        tokenEndpointAuthMethod: this.smartAuthMethod || undefined,
        jwksUri: this.jwksUrl || undefined,
        jwks: this.jwksJson || undefined,
      };
      this.serverService.createSmartClient(body).subscribe({
        next: (res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.afterSaveSmartConfig(clientId);
            onSuccess?.();
          }
        },
        error: () => this.smartConfigSaveError(),
      });
    }
  }

  private afterSaveSmartConfig(clientId: string): void {
    this.messageService.add({
      severity: 'success',
      summary: this.translate.instant('smart_launcher.cfg_save_success'),
    });
    this.loadSmartConfig(clientId);
    this.loadOauthClients();
  }

  private smartConfigSaveError(): void {
    this.messageService.add({
      severity: 'error',
      summary: this.translate.instant('smart_launcher.cfg_save_fail'),
    });
  }

  get fhirBaseUrl(): string {
    if (!this.proxyName) return '';
    const host = typeof window !== 'undefined' ? window.location.origin : '';
    const path = !this.selectedFhirBasePath || this.selectedFhirBasePath === '/' ? '' : this.selectedFhirBasePath;
    return `${host}/smart-on-fhir/${this.proxyName}${path}`;
  }

  get isStandaloneLaunch(): boolean {
    return this.launchType.includes('standalone');
  }

  get launchBoxUrl(): string {
    if (this.isStandaloneLaunch || this.launchType === 'backend_services') {
      return this.fhirBaseUrl;
    }
    return this.appLaunchUrl;
  }

  /** sim_error 下拉的分組選項（label 為 i18n key，依 launchType 過濾），供 p-select [group] 使用 */
  get simErrorOptions(): { label: string; items: { label: string; value: string }[] }[] {
    const groups: { label: string; items: { label: string; value: string }[] }[] = [];
    const isBackend = this.launchType === 'backend_services';
    if (!isBackend) {
      groups.push({
        label: 'smart_launcher.sim_error_group_auth',
        items: [
          { label: 'smart_launcher.sim_error_auth_invalid_client_id', value: 'auth_invalid_client_id' },
          { label: 'smart_launcher.sim_error_auth_invalid_redirect_uri', value: 'auth_invalid_redirect_uri' },
          { label: 'smart_launcher.sim_error_auth_invalid_scope', value: 'auth_invalid_scope' },
          { label: 'smart_launcher.sim_error_auth_invalid_client_secret', value: 'auth_invalid_client_secret' },
        ],
      });
    }
    const tokenItems = [
      { label: 'smart_launcher.sim_error_token_invalid_token', value: 'token_invalid_token' },
      { label: 'smart_launcher.sim_error_token_invalid_scope', value: 'token_invalid_scope' },
    ];
    if (!isBackend) {
      tokenItems.push({ label: 'smart_launcher.sim_error_token_expired_refresh', value: 'token_expired_refresh_token' });
    }
    groups.push({ label: 'smart_launcher.sim_error_group_token', items: tokenItems });
    groups.push({
      label: 'smart_launcher.sim_error_group_request',
      items: [
        { label: 'smart_launcher.sim_error_request_invalid_token', value: 'request_invalid_token' },
        { label: 'smart_launcher.sim_error_request_expired_token', value: 'request_expired_token' },
      ],
    });
    return groups;
  }

  onLaunchTypeChange(): void {
    const lt = this.launchType;

    this.launchTypeDescription = this.launchTypeDescriptions[lt];
    this.showPkce = lt !== 'backend_services';
    this.showProvider = lt === 'provider_ehr' || lt === 'provider_standalone';

    if (lt === 'backend_services') {
      this.clientType = 'confidential-asymmetric';
    } else if (this.clientType === 'backend-service') {
      this.clientType = 'public';
    }

    this.onClientTypeChange();
    this.resetDefaultScope();
  }

  onClientTypeChange(): void {
    this.showClientSecret = this.clientType === 'confidential-symmetric';
    this.showJwks = this.clientType === 'confidential-asymmetric';
  }

  // ── Scope ──

  private get chipValues(): Set<string> {
    return new Set(this.scopeChips.map(c => c.value));
  }

  /**
   * scope chip 切換後重建 scopeText。
   * 注意：chip.active 已由 p-toggleButton 的 [(ngModel)] 翻轉，此處只負責把
   * 「啟用的 chip」與「使用者自填的非 chip scope」合併回 scopeText。
   */
  onScopeChipsChange(): void {
    const knownValues = this.chipValues;
    const customScopes = this.scopeText
      .split(/\s+/)
      .filter(token => token.length > 0 && !knownValues.has(token));
    const activeChipScopes = this.scopeChips
      .filter(c => c.active)
      .map(c => c.value);
    this.scopeText = [...activeChipScopes, ...customScopes].join(' ');
  }

  onScopeTextChange(): void {
    const tokens = new Set(
      this.scopeText.split(/\s+/).filter(t => t.length > 0)
    );
    this.scopeChips.forEach(chip => {
      chip.active = tokens.has(chip.value);
    });
  }

  resetDefaultScope(): void {
    const defaults: Record<LaunchType, string> = {
      provider_ehr:        'openid profile launch patient/*.cruds',
      provider_standalone: 'openid profile patient/*.cruds',
      patient_standalone:  'openid profile launch/patient patient/*.cruds',
      patient_portal:      'openid profile launch patient/*.cruds',
      backend_services:    'system/*.cruds',
    };
    this.scopeText = defaults[this.launchType];
    this.onScopeTextChange();
  }

  // ── Patient Picker ──

  togglePatientPicker(): void {
    if (!this.proxyName) {
      this.patientSearchError = this.translate.instant('smart_launcher.error_select_proxy_first');
      this.showPatientPicker = true;
      return;
    }
    this.showPatientPicker = !this.showPatientPicker;
    if (this.showPatientPicker && this.patientResults.length === 0) {
      this.searchPatients();
    }
  }

  searchPatients(): void {
    if (!this.proxyName) return;
    this.patientPage = 1;
    this.patientNextUrl = null;
    this.patientPrevUrl = null;
    this.patientSearching = true;
    this.patientSearchError = '';
    this.fhirService.searchPatients(this.proxyName, this.selectedFhirBasePath, this.patientSearchKeyword, this.patientPageSize, 0, this.patientSearchMode).subscribe({
      next: (bundle) => this.applyPatientBundle(bundle),
      error: (err) => this.handlePatientError(err),
    });
  }

  prevPatientPage(): void {
    if (!this.patientPrevUrl) return;
    this.patientSearching = true;
    this.patientSearchError = '';
    this.fhirService.fetchByLink(this.patientPrevUrl).subscribe({
      next: (bundle) => {
        this.patientPage = Math.max(1, this.patientPage - 1);
        this.applyPatientBundle(bundle);
      },
      error: (err) => this.handlePatientError(err),
    });
  }

  nextPatientPage(): void {
    if (!this.patientNextUrl) return;
    this.patientSearching = true;
    this.patientSearchError = '';
    this.fhirService.fetchByLink(this.patientNextUrl).subscribe({
      next: (bundle) => {
        this.patientPage += 1;
        this.applyPatientBundle(bundle);
      },
      error: (err) => this.handlePatientError(err),
    });
  }

  private applyPatientBundle(bundle: Bundle): void {
    this.patientSearching = false;
    this.patientResults = (bundle.entry || [])
      .filter(e => e.resource?.resourceType === 'Patient')
      .map(e => SmartFhirService.extractPatientDisplay(e.resource as Patient));
    this.patientNextUrl = SmartFhirService.nextLink(bundle);
    this.patientPrevUrl = SmartFhirService.prevLink(bundle);
  }

  private handlePatientError(err: HttpErrorResponse): void {
    this.patientSearching = false;
    this.patientSearchError = err.status === 401
      ? this.translate.instant('smart_launcher.error_access_token_patient')
      : this.translate.instant('smart_launcher.error_search_failed', { msg: err.message || err.statusText || 'Unknown error' });
  }

  get patientHasNext(): boolean { return !!this.patientNextUrl; }
  get patientHasPrev(): boolean { return !!this.patientPrevUrl; }

  /** 多選 toggle */
  togglePatientSelection(patient: PatientDisplay): void {
    if (this.selectedPatientIdSet.has(patient.id)) {
      this.selectedPatientIdSet.delete(patient.id);
    } else {
      this.selectedPatientIdSet.add(patient.id);
    }
    this.syncPatientIdsFromSet();
  }

  isPatientSelectedInSet(patient: PatientDisplay): boolean {
    return this.selectedPatientIdSet.has(patient.id);
  }

  confirmPatientSelection(): void {
    this.syncPatientIdsFromSet();
    this.showPatientPicker = false;
    // 單選時觸發 encounter 載入；多選不載
    if (this.selectedPatientIdSet.size === 1) {
      this.loadEncounters();
    }
  }

  clearPatientSelection(): void {
    this.selectedPatientIdSet.clear();
    this.patientId = '';
  }

  private syncPatientIdsFromSet(): void {
    this.patientId = Array.from(this.selectedPatientIdSet).join(',');
  }

  /** 使用者手動編輯輸入框時 */
  onPatientIdsInputChange(): void {
    this.selectedPatientIdSet.clear();
    this.patientId.split(',').forEach(id => {
      const trimmed = id.trim();
      if (trimmed) this.selectedPatientIdSet.add(trimmed);
    });
  }

  onPatientIdBlur(): void {
    // 只有單選時 encounter 才有意義
    if (this.selectedPatientIdSet.size <= 1 && this.patientId && !this.patientId.includes(',')) {
      this.loadEncounters();
    }
  }

  // ── Provider Picker ──

  toggleProviderPicker(): void {
    if (!this.proxyName) {
      this.providerSearchError = this.translate.instant('smart_launcher.error_select_proxy_first');
      this.showProviderPicker = true;
      return;
    }
    this.showProviderPicker = !this.showProviderPicker;
    if (this.showProviderPicker && this.providerResults.length === 0) {
      this.searchProviders();
    }
  }

  searchProviders(): void {
    if (!this.proxyName) return;
    this.providerPage = 1;
    this.providerNextUrl = null;
    this.providerPrevUrl = null;
    this.providerSearching = true;
    this.providerSearchError = '';
    this.fhirService.searchPractitioners(this.proxyName, this.selectedFhirBasePath, this.providerSearchKeyword, this.providerPageSize, 0, this.providerSearchMode).subscribe({
      next: (bundle) => this.applyProviderBundle(bundle),
      error: (err) => this.handleProviderError(err),
    });
  }

  prevProviderPage(): void {
    if (!this.providerPrevUrl) return;
    this.providerSearching = true;
    this.providerSearchError = '';
    this.fhirService.fetchByLink(this.providerPrevUrl).subscribe({
      next: (bundle) => {
        this.providerPage = Math.max(1, this.providerPage - 1);
        this.applyProviderBundle(bundle);
      },
      error: (err) => this.handleProviderError(err),
    });
  }

  nextProviderPage(): void {
    if (!this.providerNextUrl) return;
    this.providerSearching = true;
    this.providerSearchError = '';
    this.fhirService.fetchByLink(this.providerNextUrl).subscribe({
      next: (bundle) => {
        this.providerPage += 1;
        this.applyProviderBundle(bundle);
      },
      error: (err) => this.handleProviderError(err),
    });
  }

  private applyProviderBundle(bundle: Bundle): void {
    this.providerSearching = false;
    this.providerResults = (bundle.entry || [])
      .filter(e => e.resource?.resourceType === 'Practitioner')
      .map(e => SmartFhirService.extractPractitionerDisplay(e.resource as Practitioner));
    this.providerNextUrl = SmartFhirService.nextLink(bundle);
    this.providerPrevUrl = SmartFhirService.prevLink(bundle);
  }

  private handleProviderError(err: HttpErrorResponse): void {
    this.providerSearching = false;
    this.providerSearchError = err.status === 401
      ? this.translate.instant('smart_launcher.error_access_token_provider')
      : this.translate.instant('smart_launcher.error_search_failed', { msg: err.message || err.statusText || 'Unknown error' });
  }

  get providerHasNext(): boolean { return !!this.providerNextUrl; }
  get providerHasPrev(): boolean { return !!this.providerPrevUrl; }

  /** 多選 toggle：勾選/取消勾選某個 provider */
  toggleProviderSelection(provider: PractitionerDisplay): void {
    if (this.selectedProviderIdSet.has(provider.id)) {
      this.selectedProviderIdSet.delete(provider.id);
    } else {
      this.selectedProviderIdSet.add(provider.id);
    }
    this.syncProviderIdsFromSet();
  }

  isProviderSelected(provider: PractitionerDisplay): boolean {
    return this.selectedProviderIdSet.has(provider.id);
  }

  /** 套用目前的選擇，關閉 picker */
  confirmProviderSelection(): void {
    this.syncProviderIdsFromSet();
    this.showProviderPicker = false;
  }

  /** 清空選擇 */
  clearProviderSelection(): void {
    this.selectedProviderIdSet.clear();
    this.providerIds = '';
  }

  /** 從 ID set 同步回逗號分隔字串（供輸入框顯示與送出） */
  private syncProviderIdsFromSet(): void {
    this.providerIds = Array.from(this.selectedProviderIdSet).join(',');
  }

  /** 使用者手動編輯輸入框時：把字串解析回 set */
  onProviderIdsInputChange(): void {
    this.selectedProviderIdSet.clear();
    this.providerIds.split(',').forEach(id => {
      const trimmed = id.trim();
      if (trimmed) this.selectedProviderIdSet.add(trimmed);
    });
  }

  // ── Encounter ──

  loadEncounters(): void {
    if (!this.proxyName || !this.patientId) {
      this.encounterResults = [];
      this.encounterId = '';
      return;
    }
    this.encounterLoading = true;
    this.encounterError = '';
    this.encounterResults = [];
    this.encounterId = '';
    this.fhirService.searchEncounters(this.proxyName, this.selectedFhirBasePath, this.patientId).subscribe({
      next: (bundle) => {
        this.encounterLoading = false;
        this.encounterResults = (bundle.entry || [])
          .filter(e => e.resource?.resourceType === 'Encounter')
          .map(e => SmartFhirService.extractEncounterDisplay(e.resource as Encounter));
        if (this.encounterMode === 'AUTO' && this.encounterResults.length > 0) {
          this.encounterId = this.encounterResults[0].id;
        }
      },
      error: (err: HttpErrorResponse) => {
        this.encounterLoading = false;
        this.encounterError = this.translate.instant('smart_launcher.error_load_encounters', { msg: err.message || err.statusText || 'Unknown error' });
      },
    });
  }

  onEncounterModeChange(): void {
    if (this.encounterMode === 'AUTO' && this.encounterResults.length > 0) {
      this.encounterId = this.encounterResults[0].id;
    } else if (this.encounterMode === 'MANUAL') {
      this.encounterId = '';
    }
  }

  selectEncounter(enc: EncounterDisplay): void {
    this.encounterId = enc.id;
  }

  // ── Launch ──

  launch(): void {
    // 按 Launch App 先存 dgr_smart_client，再啟動（無 clientId 則直接啟動）
    if (this.clientId) {
      this.saveSmartConfig(() => this.doLaunch());
    } else {
      this.doLaunch();
    }
  }

  private doLaunch(): void {
    if (!this.appLaunchUrl || !this.proxyName) return;

    const iss = encodeURIComponent(this.fhirBaseUrl);
    const sep = this.appLaunchUrl.includes('?') ? '&' : '?';

    switch (this.launchType) {
      case 'provider_standalone':
      case 'patient_standalone': {
        let url = `${this.appLaunchUrl}${sep}iss=${iss}`;
        if (this.simError) url += `&sim_error=${encodeURIComponent(this.simError)}`;
        window.open(url, '_blank');
        break;
      }

      case 'provider_ehr':
      case 'patient_portal':
        this.launchWithContext();
        break;
    }
  }

  private launchWithContext(): void {
    if (!this.proxyName || !this.appLaunchUrl) return;
    if (!this.clientId) {
      this.showError(this.translate.instant('smart_launcher.error_launch_missing_client_id'));
      return;
    }

    const form: Record<string, string> = { client_id: this.clientId };
    if (this.patientId) form['patient'] = this.patientId;
    if (this.providerIds) form['provider'] = this.providerIds;
    if (this.encounterId) form['encounter'] = this.encounterId;
    if (this.simError) form['sim_error'] = this.simError;
    // fhirUser type 依 launchType 決定：patient_portal → patient，其他 EHR 風格 → practitioner
    form['user_type'] = this.launchType === 'patient_portal' ? 'patient' : 'practitioner';

    let basicAuth: string | undefined;
    if (this.clientType === 'confidential-symmetric' && this.clientSecret) {
      basicAuth = btoa(this.clientId + ':' + this.clientSecret);
    }

    this.fhirService.createLaunch(form, basicAuth).subscribe({
      next: (data) => {
        if (data.launch) {
          if (this.simEhr && this.launchType === 'provider_ehr') {
            this.router.navigate(['ehr'], {
              relativeTo: this.route,
              queryParams: {
                app: this.appLaunchUrl,
                iss: this.fhirBaseUrl,
                launch: data.launch,
                proxy: this.proxyName,
                patient: this.patientId || '',
                provider: this.providerIds || '',
                encounter: this.encounterId || '',
                sim_error: this.simError || '',
              },
            });
          } else {
            const iss = encodeURIComponent(this.fhirBaseUrl);
            const launch = encodeURIComponent(data.launch);
            const sep = this.appLaunchUrl.includes('?') ? '&' : '?';
            let url = `${this.appLaunchUrl}${sep}iss=${iss}&launch=${launch}`;
            if (this.simError) url += `&sim_error=${encodeURIComponent(this.simError)}`;
            window.open(url, '_blank');
          }
        } else {
          this.showError(this.translate.instant('smart_launcher.error_launch_failed', { msg: data.error_description || data.error || 'Unknown error' }));
        }
      },
      error: (err: HttpErrorResponse) => {
        this.showError(this.translate.instant('smart_launcher.error_launch_failed', { msg: err.message || 'Unknown error' }));
      },
    });
  }

  /** 以 PrimeNG Toast 顯示錯誤訊息 */
  private showError(detail: string): void {
    this.messageService.add({
      severity: 'error',
      summary: this.translate.instant('smart_launcher.error_summary'),
      detail,
    });
  }

  launchSampleApp(): void {
    if (!this.proxyName) return;
    const iss = this.fhirBaseUrl;
    const params: Record<string, string> = { iss };
    if (this.clientId) params['client_id'] = this.clientId;
    if (this.scopeText) params['scope'] = this.scopeText;
    if (this.simError) params['sim_error'] = this.simError;

    if (this.isStandaloneLaunch) {
      const qs = new URLSearchParams(params).toString();
      this.router.navigateByUrl(`/lb00/lb0016/sample?${qs}`);
    } else {
      this.appLaunchUrl = `${window.location.origin}/dgrv4/ac4/lb00/lb0016/sample`;
      this.redirectUri = `${window.location.origin}/dgrv4/ac4/lb00/lb0016/sample/callback`;
      this.launch();
    }
  }

  copyFhirBaseUrl(): void {
    navigator.clipboard.writeText(this.fhirBaseUrl).then(() => {
      this.copySuccess = true;
      setTimeout(() => (this.copySuccess = false), 2000);
    });
  }
}

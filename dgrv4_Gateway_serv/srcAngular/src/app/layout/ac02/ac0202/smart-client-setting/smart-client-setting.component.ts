import { Component, Input, OnChanges, OnDestroy, OnInit, SimpleChanges } from '@angular/core';
import { UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Subscription } from 'rxjs';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { SmartClientDto } from 'src/app/models/api/ServerService/dpb0330.interface';
import {
  SMART_CLIENT_TYPES,
  SMART_LAUNCH_MODES,
  SMART_AUTO_APPROVE_OPTIONS,
  SMART_IDP_TYPES,
  SMART_SCOPE_OPTIONS,
  SMART_SCOPE_CATEGORIES,
  SmartClientTypeOption,
  SmartScopeOption,
} from 'src/app/models/api/ServerService/smart-client.constants';

/**
 * SMART Client 設定子元件。
 *
 * 嵌入 AC0202 安全 tab，接收當前選中的 clientId，
 * 提供 SMART App Launch 設定的檢視、新增、編輯、刪除功能。
 * 右側說明面板隨 focus 欄位切換，含 SVG 概念圖互動高亮。
 */
@Component({
  selector: 'app-smart-client-setting',
  standalone: false,
  templateUrl: './smart-client-setting.component.html',
  styleUrls: ['./smart-client-setting.component.css'],
})
export class SmartClientSettingComponent implements OnChanges, OnInit, OnDestroy {

  /** 由父元件（AC0202）傳入當前選中的 client ID */
  @Input() clientId: string = '';

  /** 該 client 的 SMART 設定（null = 載入中，undefined = 查無設定） */
  smartClient: SmartClientDto | null | undefined = null;

  loading = false;

  /** 三種模式：唯讀 / 新增 / 編輯 */
  mode: 'view' | 'create' | 'edit' = 'view';

  /** 當前 focus 的欄位名稱，控制說明面板內容 */
  activeField: string = 'clientType';

  /** clientType 連動：是否顯示 authMethod 欄位 */
  showAuthMethod = false;

  /** clientType 連動：是否顯示 jwk 相關欄位 */
  showJwk = false;

  /** 當前選中的 clientType 的 allowedAuthMethods（供 dropdown 選項用） */
  currentAuthMethods: string[] = [];

  /** 表單 */
  form!: UntypedFormGroup;

  /** 編輯模式的初始值，用於 PATCH 時比對哪些欄位有變動 */
  private editInitialValues: Record<string, any> = {};

  private clientTypeSub?: Subscription;

  /** scope 勾選狀態（key = scope 字串） */
  selectedScopes: Record<string, boolean> = {};

  /** 自訂 scope 輸入 */
  customScopeInput = '';

  /** 已選的自訂 scope（不在預定義清單內的） */
  customScopes: string[] = [];

  /** Redirect URI 列表（從換行分隔字串拆出） */
  redirectUriList: string[] = [];

  /** Redirect URI 輸入 */
  redirectUriInput = '';

  // 常數供 template 使用
  readonly clientTypes = SMART_CLIENT_TYPES;
  readonly launchModes = [...SMART_LAUNCH_MODES];
  readonly autoApproveOptions = [...SMART_AUTO_APPROVE_OPTIONS];
  readonly idpTypes = [...SMART_IDP_TYPES];
  readonly scopeOptions = SMART_SCOPE_OPTIONS;
  readonly scopeCategories = SMART_SCOPE_CATEGORIES;

  constructor(
    private fb: UntypedFormBuilder,
    private serverService: ServerService,
    private toolService: ToolService,
    private translate: TranslateService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
  ) {}

  ngOnInit(): void {
    this.initForm();
  }

  ngOnDestroy(): void {
    this.clientTypeSub?.unsubscribe();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['clientId'] && this.clientId) {
      this.mode = 'view';
      this.loadSmartClientSetting();
    }
  }

  // ==================== 表單初始化 ====================

  private initForm(): void {
    this.form = this.fb.group({
      clientType: ['', Validators.required],
      idpType: ['', Validators.required],
      allowedScopes: ['', Validators.required],
      redirectUris: [''],
      launchMode: ['standalone'],
      autoApprove: ['N'],
      tokenEndpointAuthMethod: [''],
      jwksUri: [''],
      jwks: [''],
      version: [null],
    });

    // clientType 變更時連動 authMethod/jwk 欄位的顯示
    this.clientTypeSub = this.form.get('clientType')!.valueChanges.subscribe(value => {
      this.updateFieldVisibility(value);
    });
  }

  /**
   * 根據 clientType 更新欄位顯示狀態。
   * 規則來自 SMART_CLIENT_TYPES 常數（對應後端 SmartClientType enum）。
   */
  private updateFieldVisibility(clientTypeKey: string): void {
    const typeOption = SMART_CLIENT_TYPES.find(t => t.key === clientTypeKey);
    if (typeOption) {
      this.showAuthMethod = typeOption.allowedAuthMethods.length > 0;
      this.showJwk = typeOption.requiresJwk;
      this.currentAuthMethods = typeOption.allowedAuthMethods;
    } else {
      this.showAuthMethod = false;
      this.showJwk = false;
      this.currentAuthMethods = [];
    }

    // 隱藏的欄位清空值
    if (!this.showAuthMethod) {
      this.form.get('tokenEndpointAuthMethod')!.setValue('');
    }
    if (!this.showJwk) {
      this.form.get('jwksUri')!.setValue('');
      this.form.get('jwks')!.setValue('');
    }
  }

  // ==================== 資料載入 ====================

  loadSmartClientSetting(): void {
    this.loading = true;
    this.smartClient = null;

    this.serverService.querySmartClientList({ keyword: this.clientId, pageNum: 0, pageSize: 1 })
      .subscribe({
        next: (res) => {
          this.loading = false;
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            const match = res.RespBody.content.find(c => c.clientId === this.clientId);
            this.smartClient = match ?? undefined;
          }
        },
        error: () => {
          this.loading = false;
          this.smartClient = undefined;
        },
      });
  }

  // ==================== 模式切換 ====================

  enterCreateMode(): void {
    this.mode = 'create';
    this.form.reset({
      clientType: '',
      idpType: '',
      allowedScopes: '',
      redirectUris: '',
      launchMode: 'standalone',
      autoApprove: 'N',
      tokenEndpointAuthMethod: '',
      jwksUri: '',
      jwks: '',
      version: null,
    });
    this.parseScopesFromString('');
    this.parseRedirectUrisFromString('');
    this.updateFieldVisibility('');
    this.activeField = 'clientType';
  }

  enterEditMode(): void {
    if (!this.smartClient) return;
    this.mode = 'edit';

    const values = {
      clientType: this.smartClient.clientType || '',
      idpType: this.smartClient.idpType || '',
      allowedScopes: this.smartClient.allowedScopes || '',
      redirectUris: this.smartClient.redirectUris || '',
      launchMode: this.smartClient.launchMode || 'standalone',
      autoApprove: this.smartClient.autoApprove || 'N',
      tokenEndpointAuthMethod: this.smartClient.tokenEndpointAuthMethod || '',
      jwksUri: this.smartClient.jwksUri || '',
      jwks: '',  // JWKS 不從查詢回傳（hasJwks 只是布林），編輯時需重新填入
      version: this.smartClient.version,
    };

    this.form.reset(values);
    this.editInitialValues = { ...values };
    this.parseScopesFromString(values.allowedScopes);
    this.parseRedirectUrisFromString(values.redirectUris);
    this.updateFieldVisibility(values.clientType);
    this.activeField = 'clientType';
  }

  cancelEdit(): void {
    this.mode = 'view';
  }

  // ==================== API 呼叫 ====================

  submitForm(): void {
    if (this.form.invalid) return;

    if (this.mode === 'create') {
      this.createSmartClient();
    } else if (this.mode === 'edit') {
      this.updateSmartClient();
    }
  }

  private createSmartClient(): void {
    const formVal = this.form.value;
    this.serverService.createSmartClient({
      clientId: this.clientId,
      clientType: formVal.clientType,
      idpType: formVal.idpType,
      allowedScopes: formVal.allowedScopes,
      redirectUris: formVal.redirectUris || undefined,
      launchMode: formVal.launchMode,
      autoApprove: formVal.autoApprove,
      tokenEndpointAuthMethod: formVal.tokenEndpointAuthMethod || undefined,
      jwksUri: formVal.jwksUri || undefined,
      jwks: formVal.jwks || undefined,
    }).subscribe({
      next: (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('smart_client.create_success'),
          });
          this.mode = 'view';
          this.loadSmartClientSetting();
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('smart_client.create_fail'),
        });
      },
    });
  }

  private updateSmartClient(): void {
    const formVal = this.form.value;

    // PATCH 語意：只送有變動的欄位
    const updateItem: Record<string, any> = {
      clientId: this.clientId,
      version: Number(formVal.version),
    };

    const fields = [
      'clientType', 'idpType', 'allowedScopes', 'redirectUris',
      'launchMode', 'autoApprove', 'tokenEndpointAuthMethod', 'jwksUri', 'jwks',
    ];

    let hasChange = false;
    for (const field of fields) {
      const current = formVal[field] ?? '';
      const initial = this.editInitialValues[field] ?? '';
      if (current !== initial) {
        // 空字串送 null 表示清除（對應後端 JsonNullable present + null）
        updateItem[field] = current === '' ? null : current;
        hasChange = true;
      }
    }

    if (!hasChange) {
      this.messageService.add({
        severity: 'info',
        summary: this.translate.instant('smart_client.no_change'),
      });
      return;
    }

    this.serverService.batchUpdateSmartClient({
      updateList: [updateItem as any],
    }).subscribe({
      next: (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.messageService.add({
            severity: 'success',
            summary: this.translate.instant('smart_client.update_success'),
          });
          this.mode = 'view';
          this.loadSmartClientSetting();
        }
      },
      error: () => {
        this.messageService.add({
          severity: 'error',
          summary: this.translate.instant('smart_client.update_fail'),
        });
      },
    });
  }

  deleteSmartClient(): void {
    if (!this.smartClient) return;

    this.confirmationService.confirm({
      message: this.translate.instant('smart_client.delete_confirm'),
      accept: () => {
        this.serverService.batchDeleteSmartClient({
          deleteList: [{
            clientId: this.clientId,
            version: Number(this.smartClient!.version),
          }],
        }).subscribe({
          next: (res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: this.translate.instant('smart_client.delete_success'),
              });
              this.smartClient = undefined;
              this.mode = 'view';
            }
          },
          error: () => {
            this.messageService.add({
              severity: 'error',
              summary: this.translate.instant('smart_client.delete_fail'),
            });
          },
        });
      },
    });
  }

  // ==================== SVG 高亮輔助 ====================

  /** 判斷 SVG 方塊是否高亮（根據 activeField 所屬的區塊） */
  isSvgActive(block: string): boolean {
    const fieldToBlock: Record<string, string> = {
      clientType: 'client',
      idpType: 'client',
      tokenEndpointAuthMethod: 'auth',
      jwksUri: 'credentials',
      jwks: 'credentials',
      allowedScopes: 'scope',
      redirectUris: 'scope',
      launchMode: 'scope',
      autoApprove: 'scope',
    };
    return fieldToBlock[this.activeField] === block;
  }

  // ==================== Scope 勾選邏輯 ====================

  getScopesByCategory(category: string): SmartScopeOption[] {
    return this.scopeOptions.filter(s => s.category === category);
  }

  toggleScope(scope: string): void {
    this.selectedScopes[scope] = !this.selectedScopes[scope];
    this.syncScopesToForm();
  }

  addCustomScope(): void {
    const input = this.customScopeInput.trim();
    if (!input) return;
    const predefined = this.scopeOptions.map(s => s.scope);
    if (!predefined.includes(input) && !this.customScopes.includes(input)) {
      this.customScopes.push(input);
      this.selectedScopes[input] = true;
      this.syncScopesToForm();
    }
    this.customScopeInput = '';
  }

  removeCustomScope(scope: string): void {
    this.customScopes = this.customScopes.filter(s => s !== scope);
    delete this.selectedScopes[scope];
    this.syncScopesToForm();
  }

  private syncScopesToForm(): void {
    const scopes = Object.entries(this.selectedScopes)
      .filter(([, v]) => v)
      .map(([k]) => k)
      .join(' ');
    this.form.get('allowedScopes')!.setValue(scopes);
  }

  private parseScopesFromString(scopeStr: string): void {
    this.selectedScopes = {};
    this.customScopes = [];
    const predefined = this.scopeOptions.map(s => s.scope);
    const parts = (scopeStr || '').split(/\s+/).filter(s => s);
    for (const s of parts) {
      this.selectedScopes[s] = true;
      if (!predefined.includes(s)) {
        this.customScopes.push(s);
      }
    }
  }

  // ==================== Redirect URI 邏輯 ====================

  addRedirectUri(): void {
    const input = this.redirectUriInput.trim();
    if (!input) return;
    if (!this.redirectUriList.includes(input)) {
      this.redirectUriList.push(input);
      this.syncRedirectUrisToForm();
    }
    this.redirectUriInput = '';
  }

  removeRedirectUri(uri: string): void {
    this.redirectUriList = this.redirectUriList.filter(u => u !== uri);
    this.syncRedirectUrisToForm();
  }

  private syncRedirectUrisToForm(): void {
    this.form.get('redirectUris')!.setValue(this.redirectUriList.join('\n'));
  }

  private parseRedirectUrisFromString(uriStr: string): void {
    this.redirectUriList = (uriStr || '').split(/\n/).map(s => s.trim()).filter(s => s);
  }
}

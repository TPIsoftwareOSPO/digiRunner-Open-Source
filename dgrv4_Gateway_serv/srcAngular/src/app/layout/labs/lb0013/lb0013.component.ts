import { map } from 'rxjs';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import {
  DPB0174Req,
  DPB0174RespItem,
} from 'src/app/models/api/ServerService/dpb0174.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0176Req } from 'src/app/models/api/ServerService/dpb0176.interface';
import { DPB0175Req } from 'src/app/models/api/ServerService/dpb0175.interface';
import { DPB0177Req } from 'src/app/models/api/ServerService/dpb0177.interface';
import { DPB0175Resp } from 'src/app/models/api/ServerService/dpb0175.interface';
import { DPB0178Req } from 'src/app/models/api/ServerService/dpb0178.interface';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertType, TxID } from 'src/app/models/common.enum';
import * as FileSaver from 'file-saver';
import dayjs from 'dayjs';
import {
  SmartOnFhirProxyDto,
  SmartOnFhirProxySearchReq,
} from 'src/app/models/api/ServerService/dpb0310.interface';
import { TranslateService } from '@ngx-translate/core';
import { SmartOnFhirProxyDiversionDto } from 'src/app/models/api/ServerService/dpb0311.interface';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { Dialog } from 'primeng/dialog';
import { DialogService } from 'primeng/dynamicdialog';
import { ClientInfoListComponent } from './client-info-list/client-info-list.component';
import { SmartOnFhirProxyUpdateDto } from 'src/app/models/api/ServerService/dpb0312.interface';
import { DPB0313ReqItem } from 'src/app/models/api/ServerService/dpb0313.interface';
import {
  StreamApiService,
  StreamResponse,
} from '../../ac03/ac0316/api-test/stream-api.service';
import { environment } from 'src/environments/environment';
import { ReqDPB0314 } from 'src/app/models/api/ServerService/dpb0314.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { StickyListComponent } from './sticky-list/sticky-list.component';

interface _SmartOnFhirProxyDto extends SmartOnFhirProxyDto {
  result?: string;
}

@Component({
  selector: 'app-lb0013',
  templateUrl: './lb0013.component.html',
  styleUrls: ['./lb0013.component.css'],
  providers: [ConfirmationService],
  standalone: false,
})
export class Lb0013Component extends BaseComponent implements OnInit {
  @ViewChild('fileInputP1') fileInputP1!: ElementRef<HTMLInputElement>;
  @ViewChild('fileInputP3') fileInputP3!: ElementRef<HTMLInputElement>;

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  formE!: FormGroup;
  cols: { field: string; header: string }[] = [];
  tableData: Array<SmartOnFhirProxyDto> = [];
  currentAction: string = '';
  totalElements: number = 0;
  currentPage: number = 0;
  btnName: string = '';
  selectedItem?: SmartOnFhirProxyDto;

  _formValid: boolean = false;
  // fileName: string = '';
  // file: any = null;

  exportProcessPercent: string = '';
  exportStatus: string = '';

  importFile?: any;
  importFileName: string = '';
  importData: _SmartOnFhirProxyDto[] = [];
  importSelected: _SmartOnFhirProxyDto[] = [];
  // Auth 下拉選項（digirunner 送後端值為小寫，畫面顯示 digiRunner）
  sofProxyAuthOptions: Array<{ label: string; value: string }> = [
    { label: 'none', value: 'none' },
    { label: 'digiRunner', value: 'digirunner' },
    { label: 'third-party', value: 'third-party' },
  ];
  // Introspection Auth 下拉選項（label 與 value 相同）
  sofProxyInIntrospectionAuthOptions: string[] = [
    'client_secret_basic',
    'bearer',
    'none',
  ];
  // Fhir Server驗證 Auth Method 下拉選項（label 與 value 相同）
  sofProxyOutAuthMethodOptions: string[] = [
    'client_secret_basic',
    'client_secret_post',
  ];
  // formFile!: FormGroup;
  reload: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private serverService: ServerService,
    private toolService: ToolService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private alertService: AlertService,
    private ngxService: NgxUiLoaderService,
    private translate: TranslateService,
    private dialogService: DialogService,
    private streamApi: StreamApiService,
    private api: ApiBaseService,
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    this.form = this.fb.group({
      keyword: new FormControl(''),
    });
    this.formE = this.fb.group({
      sofProxyId: new FormControl(''),
      sofProxyName: new FormControl('', ValidatorFns.requiredValidator()),
      sofProxyStatus: new FormControl(''),
      sofProxyStatusName: new FormControl(''),
      sofProxyRemark: new FormControl(''),
      sofProxyAuth: new FormControl('none'),
      sofProxyInIntrospectionAuth: new FormControl(''),
      sofProxyInWellKnown: new FormControl(''),
      sofProxyInIntrospectionClientId: new FormControl(''),
      sofProxyInIntrospectionSecret: new FormControl(''),
      sofProxyOutRelay: new FormControl('N'),
      sofProxyOutAuthMethod: new FormControl(''),
      sofProxyOutWellKnown: new FormControl(''),
      sofProxyOutClientId: new FormControl(''),
      sofProxyOutClientSecret: new FormControl(''),
      sofProxyOutScope: new FormControl(''),
      sofProxySqlInjection: new FormControl(''),
      sofProxyTraffic: new FormControl(''),
      sofProxyXss: new FormControl(''),
      sofProxyXxe: new FormControl(''),
      sofProxyTps: new FormControl(''),
      sofProxyIgnoreApi: new FormControl(''),
      sofProxyClientId: new FormControl([]),
      sofProxyShowLog: new FormControl(''),
      sofProxySticky: new FormControl(''),
      diversionList: new FormControl(''),
      createDateTime: new FormControl(''),
      createUser: new FormControl(''),
      updateDateTime: new FormControl(''),
      updateUser: new FormControl(''),
      version: new FormControl(''),
    });

    // this.formFile = this.fb.group({
    //   file: new FormControl(),
    //   fileName: new FormControl({ value: '', disabled: true }),
    // });

    this.cols = [
      { field: 'sofProxyStatus', header: this.translate.instant('sof.status') },
      { field: 'sofProxyId', header: 'ID' },
      { field: 'sofProxyName', header: this.translate.instant('sof.name') },
      { field: 'sofProxyRemark', header: this.translate.instant('memo') },
      { field: 'sofProxyAuth', header: 'Auth' },
      {
        field: 'sofProxySqlInjection',
        header: this.translate.instant('sof.sql_injection'),
      },
      {
        field: 'sofProxyTraffic',
        header: this.translate.instant('sof.traffic'),
      },
      { field: 'sofProxyXss', header: this.translate.instant('sof.xss') },
      { field: 'sofProxyXxe', header: this.translate.instant('sof.xxe') },
      {
        field: 'sofProxyShowLog',
        header: this.translate.instant('sof.show_log'),
      },
      {
        field: 'sofProxySticky',
        header: this.translate.instant('sof.sticky'),
      },
      { field: 'sofProxyTps', header: 'TPS' },
      {
        field: 'sofProxyIgnoreApi',
        header: this.translate.instant('sof.ignore_api'),
      },
      {
        field: 'sofProxyClientId',
        header: this.translate.instant('sof.client_id'),
      },
      {
        field: 'diversionList',
        header: this.translate.instant('sof.diversion_list'),
      },
      {
        field: 'updateDateTime',
        header: this.translate.instant('update_time'),
      },
      { field: 'updateUser', header: this.translate.instant('update_user') },
    ];

    this.serverService
      .querySmartOnFhirProxyList_ignore1298({})
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.content;
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;
        } else {
          this.tableData = [];
        }
      });

    this.sofProxyTps.valueChanges.subscribe((val) => {
      if (val == null) this.sofProxyTps.setValue('');
    });

    // third-party 時 introspection 四欄位為必填；切回其他 Auth 模式時清空值並移除驗證
    this.sofProxyAuth.valueChanges.subscribe((val) => {
      const introspectionCtrls = [
        this.sofProxyInIntrospectionAuth,
        this.sofProxyInWellKnown,
        this.sofProxyInIntrospectionClientId,
        this.sofProxyInIntrospectionSecret,
      ];
      introspectionCtrls.forEach((ctrl) => {
        if (val === 'third-party') {
          ctrl.setValidators(ValidatorFns.requiredValidator());
        } else {
          ctrl.clearValidators();
          ctrl.setValue('');
        }
        ctrl.updateValueAndValidity();
      });
      // third-party 時 Introspection Auth 下拉預設 client_secret_basic
      if (val === 'third-party' && !this.sofProxyInIntrospectionAuth.value) {
        this.sofProxyInIntrospectionAuth.setValue('client_secret_basic');
      }
      // Fhir Server驗證（sofProxyOutRelay）僅 Auth = none 時可操作；
      // 其餘模式強制設為 N 並 disable（觸發既有 valueChanges 清空 out 欄位）
      if (val === 'none') {
        // detail 模式整份表單為 disabled，不得在此重新啟用
        if (this.currentAction !== 'detail') {
          this.sofProxyOutRelay.enable({ emitEvent: false });
        }
      } else {
        if (this.sofProxyOutRelay.value !== 'N') {
          this.sofProxyOutRelay.setValue('N');
        }
        this.sofProxyOutRelay.disable({ emitEvent: false });
      }
    });

    // Fhir Server驗證（sofProxyOutRelay）= Y 時 Client ID / Client Secret 為必填；
    // 取消勾選時清空五個 out 欄位並移除驗證
    this.sofProxyOutRelay.valueChanges.subscribe((val) => {
      const requiredCtrls = [this.sofProxyOutClientId, this.sofProxyOutClientSecret];
      const outCtrls = [
        this.sofProxyOutAuthMethod,
        this.sofProxyOutWellKnown,
        this.sofProxyOutClientId,
        this.sofProxyOutClientSecret,
        this.sofProxyOutScope,
      ];
      if (val === 'Y') {
        requiredCtrls.forEach((ctrl) =>
          ctrl.setValidators(ValidatorFns.requiredValidator()),
        );
        // 啟用 Fhir Server驗證時 Auth Method 下拉預設 client_secret_basic
        if (!this.sofProxyOutAuthMethod.value) {
          this.sofProxyOutAuthMethod.setValue('client_secret_basic');
        }
      } else {
        requiredCtrls.forEach((ctrl) => ctrl.clearValidators());
        outCtrls.forEach((ctrl) => ctrl.setValue(''));
      }
      outCtrls.forEach((ctrl) => ctrl.updateValueAndValidity());
    });
  }

  querySmartOnFhirProxyList() {
    this.currentPage = 0;
    let ReqBody = {
      keywords: this.keyword.value
        ? this.keyword.value.toString().split(' ')
        : [],
    } as SmartOnFhirProxySearchReq;
    this.ngxService.start();
    this.serverService.querySmartOnFhirProxyList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.content;
        this.totalElements = res.RespBody.totalElements;
        this.currentPage = res.RespBody.number + 1;
      } else {
        this.tableData = [];
      }
      this.ngxService.stop();
    });
  }

  getMoreData() {
    this.ngxService.start();
    this.serverService
      .querySmartOnFhirProxyList({
        pageNum: this.currentPage,
        pageSize: 20,
        sortOrder: 'desc',
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = this.tableData.concat(res.RespBody.content);
          this.totalElements = res.RespBody.totalElements;
          this.currentPage = res.RespBody.number + 1;
        } else {
          this.tableData = [];
        }
        this.ngxService.stop();
      });
  }

  headerReturn() {
    if (this.reload) {
      this.reload = false;
      this.querySmartOnFhirProxyList();
    }
    this.changePage('query');
  }

  async changePage(action: string, rowData?: SmartOnFhirProxyDto) {
    this.currentAction = action;
    const code = [
      'button.create',
      'button.update',
      'button.delete',
      'button.detail',
      'cfm_del',
      'button.import',
    ];
    const dict = await this.toolService.getDict(code);
    this.formE.enable();
    // this.resetFormValidator(this.formE);
    this.formE.reset('');
    this.formE.clearValidators();
    this.formE.updateValueAndValidity();
    this.diversionList.setValue([]);
    this.sofProxyClientId.setValue([]);
    this.sofProxyClientId.disable();
    this._formValid = false;
    this.selectedItem = undefined;
    this.importFile = undefined;
    this.importFileName = '';
    this.importData = [];
    this.exportProcessPercent = '';
    this.exportStatus = '';

    switch (action) {
      case 'import':
        this.currentTitle = `${this.title} > ${dict['button.import']}`;
        this.pageNum = 3;
        break;
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.btnName = dict['button.create'];
        this.sofProxyStatus.setValue('N');
        this.sofProxyAuth.setValue('none');
        this.sofProxyOutRelay.setValue('N');
        this.sofProxyTps.setValue(0);

        break;

      case 'update':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 2;
        this.btnName = dict['button.update'];
        this.selectedItem = rowData;

        this.sofProxyId.setValue(this.selectedItem!.sofProxyId);
        this.sofProxyName.setValue(this.selectedItem!.sofProxyName);
        this.sofProxyStatus.setValue(this.selectedItem!.sofProxyStatus);
        this.sofProxyRemark.setValue(this.selectedItem!.sofProxyRemark);
        this.sofProxyAuth.setValue(
          this.selectedItem!.sofProxyAuth?.[0] ?? 'none',
        );
        // 需在 sofProxyAuth 之後帶入，避免被切換 Auth 模式的清空邏輯洗掉
        this.sofProxyInIntrospectionAuth.setValue(
          this.selectedItem!.sofProxyInIntrospectionAuth ||
            'client_secret_basic',
        );
        this.sofProxyInWellKnown.setValue(
          this.selectedItem!.sofProxyInWellKnown ?? '',
        );
        this.sofProxyInIntrospectionClientId.setValue(
          this.selectedItem!.sofProxyInIntrospectionClientId ?? '',
        );
        // DPB0310 不會回傳 secret，帶入時固定空白
        this.sofProxyInIntrospectionSecret.setValue('');
        // Auth 非 none 時 relay 已被連動邏輯強制為 N/disable，不再帶入舊資料
        if ((this.selectedItem!.sofProxyAuth?.[0] ?? 'none') === 'none') {
          this.sofProxyOutRelay.setValue(
            this.selectedItem!.sofProxyOutRelay ?? 'N',
          );
          this.sofProxyOutAuthMethod.setValue(
            this.selectedItem!.sofProxyOutAuthMethod || 'client_secret_basic',
          );
          this.sofProxyOutWellKnown.setValue(
            this.selectedItem!.sofProxyOutWellKnown ?? '',
          );
          this.sofProxyOutClientId.setValue(
            this.selectedItem!.sofProxyOutClientId ?? '',
          );
          // 後端不回傳 client secret（以 hasOutClientSecret 遮罩），帶入時固定空白
          this.sofProxyOutClientSecret.setValue('');
          this.sofProxyOutScope.setValue(
            this.selectedItem!.sofProxyOutScope ?? '',
          );
        }
        this.sofProxySqlInjection.setValue(
          this.selectedItem!.sofProxySqlInjection,
        );
        this.sofProxyTraffic.setValue(this.selectedItem!.sofProxyTraffic);
        this.sofProxyXss.setValue(this.selectedItem!.sofProxyXss);
        this.sofProxyXxe.setValue(this.selectedItem!.sofProxyXxe);
        this.sofProxyShowLog.setValue(this.selectedItem!.sofProxyShowLog);
        this.sofProxySticky.setValue(this.selectedItem!.sofProxySticky);
        this.sofProxyTps.setValue(this.selectedItem!.sofProxyTps);
        this.sofProxyIgnoreApi.setValue(this.selectedItem!.sofProxyIgnoreApi);
        this.sofProxyClientId.setValue(this.selectedItem!.sofProxyClientId);

        this.diversionList.setValue(this.selectedItem!.diversionList);

        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.pageNum = 2;
        this.btnName = dict['button.detail'];
        this.selectedItem = rowData;

        this.formE.disable();
        this.sofProxyId.setValue(this.selectedItem!.sofProxyId);
        this.sofProxyName.setValue(this.selectedItem!.sofProxyName);
        this.sofProxyStatus.setValue(this.selectedItem!.sofProxyStatus);
        this.sofProxyRemark.setValue(this.selectedItem!.sofProxyRemark);
        this.sofProxyAuth.setValue(
          this.selectedItem!.sofProxyAuth?.[0] ?? 'none',
        );
        // 需在 sofProxyAuth 之後帶入，避免被切換 Auth 模式的清空邏輯洗掉
        this.sofProxyInIntrospectionAuth.setValue(
          this.selectedItem!.sofProxyInIntrospectionAuth ||
            'client_secret_basic',
        );
        this.sofProxyInWellKnown.setValue(
          this.selectedItem!.sofProxyInWellKnown ?? '',
        );
        this.sofProxyInIntrospectionClientId.setValue(
          this.selectedItem!.sofProxyInIntrospectionClientId ?? '',
        );
        // DPB0310 不會回傳 secret，帶入時固定空白
        this.sofProxyInIntrospectionSecret.setValue('');
        // Auth 非 none 時 relay 已被連動邏輯強制為 N/disable，不再帶入舊資料
        if ((this.selectedItem!.sofProxyAuth?.[0] ?? 'none') === 'none') {
          this.sofProxyOutRelay.setValue(
            this.selectedItem!.sofProxyOutRelay ?? 'N',
          );
          this.sofProxyOutAuthMethod.setValue(
            this.selectedItem!.sofProxyOutAuthMethod || 'client_secret_basic',
          );
          this.sofProxyOutWellKnown.setValue(
            this.selectedItem!.sofProxyOutWellKnown ?? '',
          );
          this.sofProxyOutClientId.setValue(
            this.selectedItem!.sofProxyOutClientId ?? '',
          );
          // 後端不回傳 client secret（以 hasOutClientSecret 遮罩），帶入時固定空白
          this.sofProxyOutClientSecret.setValue('');
          this.sofProxyOutScope.setValue(
            this.selectedItem!.sofProxyOutScope ?? '',
          );
        }
        this.sofProxySqlInjection.setValue(
          this.selectedItem!.sofProxySqlInjection,
        );
        this.sofProxyTraffic.setValue(this.selectedItem!.sofProxyTraffic);
        this.sofProxyXss.setValue(this.selectedItem!.sofProxyXss);
        this.sofProxyXxe.setValue(this.selectedItem!.sofProxyXxe);
        this.sofProxyShowLog.setValue(this.selectedItem!.sofProxyShowLog);
        this.sofProxySticky.setValue(this.selectedItem!.sofProxySticky);
        this.sofProxyTps.setValue(this.selectedItem!.sofProxyTps);
        this.sofProxyIgnoreApi.setValue(this.selectedItem!.sofProxyIgnoreApi);
        this.sofProxyClientId.setValue(this.selectedItem!.sofProxyClientId);

        this.diversionList.setValue(this.selectedItem!.diversionList);

        break;
      case 'delete':
        this.confirmationService.confirm({
          header: ' ',
          message: dict['cfm_del'],
          accept: () => {
            this.deleteConfirm(rowData!);
          },
        });
        break;
      case 'sticky':
        const ref = this.dialogService.open(StickyListComponent, {
          data: {
            data: rowData ,
            showMessage: (summary:string = '', detail:string = '')=> this.showMessage(summary,detail)
          },
          header: 'Sticky List',
          styleClass: 'cHeader cContent cIcon',
          width: '700px',
          // height: '100vh'
        });

        ref?.onClose.subscribe((res) => {
          if (res) {
            this.sofProxyClientId!.setValue(
              res.map((row: any) => row.clientId),
            );
          } else {
            this.sofProxyClientId!.setValue([]);
          }
        });
        break;
    }
  }

  openStickyList(rowData: SmartOnFhirProxyDto) {
    const ref = this.dialogService.open(StickyListComponent, {
      data: { data: rowData },
      header: 'Sticky List',
      styleClass: 'cHeader cContent cIcon',
      width: '700px',
      // height: '100vh'
    });

        ref?.onClose.subscribe((res) => {
          if (res) {
            this.sofProxyClientId!.setValue(
              res.map((row: any) => row.clientId),
            );
          } else {
            this.sofProxyClientId!.setValue([]);
          }
        });
  }

  async procData() {
    const code = ['message.create', 'key', 'message.success', 'message.update'];
    const dict = await this.toolService.getDict(code);

    switch (this.currentAction) {
      case 'create':
        let reqBodyC = {
          sofProxyName: this.sofProxyName.value,
          sofProxyStatus: this.sofProxyStatus.value,
          sofProxyRemark: this.sofProxyRemark.value,
          // 後端仍需 sofProxyAccessToken 欄位，無 UI、固定送 'N'
          sofProxyAccessToken: 'N',
          sofProxyAuth: [this.sofProxyAuth.value || 'none'],
          sofProxyOutRelay: this.sofProxyOutRelay.value == 'Y' ? 'Y' : 'N',
          // Fhir Server驗證啟用時才送後端代取 token 相關欄位
          ...(this.sofProxyOutRelay.value === 'Y'
            ? {
                sofProxyOutAuthMethod: this.sofProxyOutAuthMethod.value,
                sofProxyOutWellKnown: this.sofProxyOutWellKnown.value,
                sofProxyOutClientId: this.sofProxyOutClientId.value,
                sofProxyOutClientSecret: this.sofProxyOutClientSecret.value,
                sofProxyOutScope: this.sofProxyOutScope.value,
              }
            : {}),
          // third-party 時才送 introspection 相關欄位
          ...(this.sofProxyAuth.value === 'third-party'
            ? {
                sofProxyInIntrospectionAuth:
                  this.sofProxyInIntrospectionAuth.value,
                sofProxyInWellKnown: this.sofProxyInWellKnown.value,
                sofProxyInIntrospectionClientId:
                  this.sofProxyInIntrospectionClientId.value,
                sofProxyInIntrospectionSecret:
                  this.sofProxyInIntrospectionSecret.value,
              }
            : {}),
          sofProxySqlInjection:
            this.sofProxySqlInjection.value == 'Y' ? 'Y' : 'N',
          sofProxyTraffic: this.sofProxyTraffic.value == 'Y' ? 'Y' : 'N',
          sofProxyXss: this.sofProxyXss.value == 'Y' ? 'Y' : 'N',
          sofProxyXxe: this.sofProxyXxe.value == 'Y' ? 'Y' : 'N',
          sofProxyShowLog: this.sofProxyShowLog.value == 'Y' ? 'Y' : 'N',
          sofProxySticky: this.sofProxySticky.value == 'Y' ? 'Y' : 'N',
          sofProxyTps: this.sofProxyTps.value,
          sofProxyIgnoreApi: this.sofProxyIgnoreApi.value,
          sofProxyClientId: this.sofProxyClientId.value,
          diversionList:
            this.diversionList.value?.map((row: any) => {
              return {
                sofProxyDiversionProbability: row.sofProxyDiversionProbability,
                sofProxyDiversionUrl: row.sofProxyDiversionUrl,
              } as SmartOnFhirProxyDiversionDto;
            }) ?? [],
        };

        this.serverService.addSmartOnFhirProxy(reqBodyC).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.create']}`,
              detail: `${dict['message.create']} ${dict['message.success']}!`,
            });
            this.changePage('query');
            this.querySmartOnFhirProxyList();
          }
        });

        break;
      case 'update':
        let reqBodyU = {
          sofProxyId: this.sofProxyId.value,
          version: this.selectedItem?.version,
          sofProxyName: this.sofProxyName.value,
          sofProxyStatus: this.sofProxyStatus.value,
          sofProxyRemark: this.sofProxyRemark.value,

          // 後端仍需 sofProxyAccessToken 欄位，無 UI、固定送 'N'
          sofProxyAccessToken: 'N',
          sofProxyAuth: [this.sofProxyAuth.value || 'none'],
          sofProxyOutRelay: this.sofProxyOutRelay.value == 'Y' ? 'Y' : 'N',
          // Fhir Server驗證啟用時才送後端代取 token 相關欄位
          ...(this.sofProxyOutRelay.value === 'Y'
            ? {
                sofProxyOutAuthMethod: this.sofProxyOutAuthMethod.value,
                sofProxyOutWellKnown: this.sofProxyOutWellKnown.value,
                sofProxyOutClientId: this.sofProxyOutClientId.value,
                sofProxyOutClientSecret: this.sofProxyOutClientSecret.value,
                sofProxyOutScope: this.sofProxyOutScope.value,
              }
            : {}),
          // third-party 時才送 introspection 相關欄位
          ...(this.sofProxyAuth.value === 'third-party'
            ? {
                sofProxyInIntrospectionAuth:
                  this.sofProxyInIntrospectionAuth.value,
                sofProxyInWellKnown: this.sofProxyInWellKnown.value,
                sofProxyInIntrospectionClientId:
                  this.sofProxyInIntrospectionClientId.value,
                sofProxyInIntrospectionSecret:
                  this.sofProxyInIntrospectionSecret.value,
              }
            : {}),
          sofProxySqlInjection:
            this.sofProxySqlInjection.value == 'Y' ? 'Y' : 'N',
          sofProxyTraffic: this.sofProxyTraffic.value == 'Y' ? 'Y' : 'N',
          sofProxyXss: this.sofProxyXss.value == 'Y' ? 'Y' : 'N',
          sofProxyXxe: this.sofProxyXxe.value == 'Y' ? 'Y' : 'N',
          sofProxyShowLog: this.sofProxyShowLog.value == 'Y' ? 'Y' : 'N',
          sofProxySticky: this.sofProxySticky.value == 'Y' ? 'Y' : 'N',
          sofProxyTps: this.sofProxyTps.value,
          sofProxyIgnoreApi: this.sofProxyIgnoreApi.value,
          sofProxyClientId: this.sofProxyClientId.value,
          diversionList: this.diversionList.value?.map((row: any) => {
            return {
              sofProxyDiversionId: row.sofProxyDiversionId ?? '',
              version: row.version ?? '',
              sofProxyDiversionUrl: row.sofProxyDiversionUrl,
              sofProxyDiversionProbability: row.sofProxyDiversionProbability,
            } as SmartOnFhirProxyDiversionDto;
          }),
        } as SmartOnFhirProxyUpdateDto;

        this.serverService
          .batchUpdateSmartOnFhirProxy({ proxyList: [reqBodyU] })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']}`,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });
              this.changePage('query');
              this.querySmartOnFhirProxyList();
            }
          });

        break;
    }
  }

  // DPB0341: 以目前輸入的 out_* 代取設定測試取 token（不寫 DB），依結果彈出提示
  async testRelayToken() {
    const dict = await this.toolService.getDict([
      'message.success',
      'message.fail',
    ]);
    this.ngxService.start();
    this.serverService
      .testSmartOnFhirProxyRelay({
        sofProxyOutAuthMethod: this.sofProxyOutAuthMethod.value,
        sofProxyOutClientId: this.sofProxyOutClientId.value,
        sofProxyOutClientSecret: this.sofProxyOutClientSecret.value,
        sofProxyOutWellKnown: this.sofProxyOutWellKnown.value,
        // 無輸入框：更新時沿用既有 token endpoint，建立時留空由後端從 well-known 解析
        sofProxyOutTokenEndpoint:
          this.selectedItem?.sofProxyOutTokenEndpoint ?? '',
        sofProxyOutScope: this.sofProxyOutScope.value,
      })
      .subscribe((res) => {
        this.ngxService.stop();
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const body = res.RespBody;
          if (body.success && body.tokenObtained) {
            // 代取成功且取得 token：顯示實際 endpoint 與有效秒數
            this.alertService.ok(
              dict['message.success'],
              body.message,
              AlertType.success,
              `tokenEndpoint: ${body.tokenEndpoint}<br>expiresInSec: ${body.expiresInSec}`,
            );
          } else if (body.success && !body.tokenObtained) {
            // 代取流程成功但未取得 token
            this.alertService.ok(
              dict['message.success'],
              body.message,
              AlertType.warning,
            );
          } else {
            this.alertService.ok(
              dict['message.fail'],
              body.message,
              AlertType.error,
            );
          }
        }
      });
  }

  deleteConfirm(rowData: SmartOnFhirProxyDto) {
    let reqBodyD = {
      sofProxyId: rowData.sofProxyId,
      version: rowData.version,
    } as DPB0313ReqItem;

    this.serverService
      .deleteSmartOnFhirProxy({ proxyList: [reqBodyD] })
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['message.delete', 'message.success'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: `${dict['message.delete']}`,
            detail: `${dict['message.delete']} ${dict['message.success']}!`,
          });
          this.querySmartOnFhirProxyList();
        }
      });
  }

  // async fileChange(event: any) {
  //   let file: FileList = event.target.files;

  //   if (file.length != 0) {
  //     this.file = file.item(0);
  //     this.fileName = file[0].name;
  //     event.target.value = '';
  //   } else {
  //     this.file = null;
  //     event.target.value = '';
  //   }
  // }

  openFileBrowser() {
    // 依目前 pageNum 觸發對應的 input，修正舊版 jQuery $('#file') 因重複 id 只抓到第一個元素、
    // 導致在 pageNum==3 誤觸發 pageNum==1 input 的既有 bug
    if (this.pageNum === 3) {
      this.fileInputP3.nativeElement.click();
    } else {
      this.fileInputP1.nativeElement.click();
    }
  }

  exportWebsocketProxy() {
    this.ngxService.start();
    this.serverService.exportWebsocketProxy().subscribe((res) => {
      if (res.type === 'application/json') {
        const reader = new FileReader();
        reader.onload = () => {
          const jsonData = JSON.parse(reader.result as string);
          this.alertService.ok(
            jsonData.ResHeader.rtnMsg,
            '',
            AlertType.warning,
            jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID,
          );
        };
        reader.readAsText(res);
      } else {
        const data: Blob = new Blob([res], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
        });

        const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
        const ver = sessionStorage.getItem('majorVersionNo') ?? '';
        FileSaver.saveAs(data, `WebSocket_${date}.xlsx`);
      }
      this.ngxService.stop();
    });
  }

  formatDiversionList(diversionDto: Array<SmartOnFhirProxyDiversionDto>) {
    if (diversionDto) {
      return diversionDto.map((row) => row.sofProxyDiversionId).join(', ');
    } else return '';
  }

  formValid(evt) {
    this._formValid = evt;
  }

  queryRoleList() {
    const ref = this.dialogService.open(ClientInfoListComponent, {
      data: { selectionMode: 'single' },
      header: this.translate.instant('role_list'),
      styleClass: 'cHeader cContent cIcon',
      width: '700px',
    });

    ref?.onClose.subscribe((res) => {
      if (res) {
        this.sofProxyClientId!.setValue(res.map((row: any) => row.clientId));
      } else {
        this.sofProxyClientId!.setValue([]);
      }
    });
  }

  exportFile() {
    let tarUrl = '';
    if (!environment.production) {
      tarUrl = environment.apiUrl;
    } else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    const _filePath = `${tarUrl}/dgrv4/11/DPB0314`;
    let token = this.toolService.getToken();

    let body = {
      ReqHeader: this.api.getReqHeader(TxID.exportSmartOnFhirProxy),
      ReqBody: {},
    } as ReqDPB0314;

    let _exportData = [];
    // this.ngxService.start();
    this.streamApi
      .getStreamMessages('POST', _filePath, JSON.stringify(body), {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
      })
      .subscribe({
        next: (res) => {
          this.exportProcessPercent = res.percentage;
          this.exportStatus = res.type;
          if (res.type === 'progress') {
            _exportData = _exportData.concat(res.data);
          } else if (res.type === 'complete') {
            this.exportFileProcess(_exportData);
          }
        },

        complete: () => {
          // console.log('export complete');
          // this.ngxService.stop();
        },

        error: (err) => {
          console.error(err);
          this.ngxService.stop();
        },
      });
  }

  importFileProcess() {
    // console.log(this.importSelected);
    let fileReader = new FileReader();
    try {
      fileReader.onloadend = () => {
        let tarUrl = '';
        if (!environment.production) {
          tarUrl = environment.apiUrl;
        } else {
          tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
        }
        const _filePath = `${tarUrl}/dgrv4/11/DPB0316`;
        let token = this.toolService.getToken();

        let formData = new FormData();
        formData.append('jsonData', JSON.stringify(this.importSelected));

        this.streamApi
          .getStreamMessages('POST', _filePath, formData, {
            Authorization: `Bearer ${token}`,
          })
          .subscribe({
            next: (res) => {
              // console.log(res);
              if (res.type === 'progress') {
                //   this.importData = this.importData.concat(res.data);
                this.importData.find((row) => {
                  if (row.sofProxyId === res.data.sofProxyId)
                    row.result = res.data.validationResult.success;
                });
              }
              // else if (res.type === 'complete') {
              //   this.exportFileProcess(_exportData);
              // }
            },

            complete: () => {
              this.ngxService.stop();
            },

            error: (err) => {
              // console.error(err);
              this.ngxService.stop();
            },
          });
      };
    } catch (error) {
      // console.log(error);
      this.ngxService.stop();
    }
    this.ngxService.start();
    fileReader.readAsText(this.importFile!);
  }

  showMessage(summary:string = '', detail:string = '') {
    this.messageService.add({
      severity: 'success',
      summary: summary,
      detail: detail,
    });
  }

  exportFileProcess(data: any) {
    if (!data || (Array.isArray(data) && data.length === 0)) {
      this.messageService.add({
        severity: 'warn',
        summary: 'No Data',
        detail: 'There is no data to export.',
      });
      return;
    }

    try {
      const jsonString = JSON.stringify(data, null, 2);
      const blob = new Blob([jsonString], {
        type: 'application/json;charset=utf-8',
      });
      const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
      FileSaver.saveAs(blob, `exported_data_${date}.json`);
    } catch (error) {
      console.error('Error during file export:', error);
      this.messageService.add({
        severity: 'error',
        summary: 'Export Failed',
        detail: 'Could not export data to JSON file.',
      });
    }
  }

  changeFile(event: any) {
    let file: FileList = event.target.files;

    if (file.length != 0) {
      this.importFile = file.item(0);
      this.importFileName = file[0].name;
      event.target.value = '';
    } else {
      this.importFile = undefined;
      this.importFileName = '';
      event.target.value = '';
    }
  }

  uploadFile() {
    this.importData = [];
    this.importSelected = [];
    let fileReader = new FileReader();
    try {
      fileReader.onloadend = () => {
        let tarUrl = '';
        if (!environment.production) {
          tarUrl = environment.apiUrl;
        } else {
          tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
        }
        const _filePath = `${tarUrl}/dgrv4/11/DPB0315`;
        let token = this.toolService.getToken();

        let formData = new FormData();
        formData.append('file', this.importFile!);

        this.streamApi
          .getStreamMessages('POST', _filePath, formData, {
            Authorization: `Bearer ${token}`,
          })
          .subscribe({
            next: (res) => {
              // console.log(res);
              if (res.type === 'progress') {
                this.importData = this.importData.concat(res.data);
              }
              // else if (res.type === 'complete') {
              //   this.exportFileProcess(_exportData);
              // }
            },

            complete: () => {
              this.reload = true;
              this.ngxService.stop();
            },

            error: (err) => {
              console.error(err);
              this.ngxService.stop();
            },
          });
      };
    } catch (error) {
      console.log(error);
      this.ngxService.stop();
    }
    this.ngxService.start();
    fileReader.readAsText(this.importFile!);
  }

  /** 將後端回傳的 Auth 值轉為畫面顯示文字（無對應選項時直接顯示原值） */
  getSofProxyAuthLabel(values?: Array<string>): string {
    return (values ?? [])
      .map(
        (val) =>
          this.sofProxyAuthOptions.find((opt) => opt.value === val)?.label ??
          val,
      )
      .join(', ');
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get sofProxyId() {
    return this.formE.get('sofProxyId')!;
  }

  public get sofProxyName() {
    return this.formE.get('sofProxyName')!;
  }

  public get sofProxyStatus() {
    return this.formE.get('sofProxyStatus')!;
  }

  public get sofProxyStatusName() {
    return this.formE.get('sofProxyStatusName')!;
  }

  public get sofProxyRemark() {
    return this.formE.get('sofProxyRemark')!;
  }

  public get sofProxyAuth() {
    return this.formE.get('sofProxyAuth')!;
  }

  public get sofProxyInIntrospectionAuth() {
    return this.formE.get('sofProxyInIntrospectionAuth')!;
  }

  public get sofProxyInWellKnown() {
    return this.formE.get('sofProxyInWellKnown')!;
  }

  public get sofProxyInIntrospectionClientId() {
    return this.formE.get('sofProxyInIntrospectionClientId')!;
  }

  public get sofProxyInIntrospectionSecret() {
    return this.formE.get('sofProxyInIntrospectionSecret')!;
  }

  public get sofProxyOutRelay() {
    return this.formE.get('sofProxyOutRelay')!;
  }

  public get sofProxyOutAuthMethod() {
    return this.formE.get('sofProxyOutAuthMethod')!;
  }

  public get sofProxyOutWellKnown() {
    return this.formE.get('sofProxyOutWellKnown')!;
  }

  public get sofProxyOutClientId() {
    return this.formE.get('sofProxyOutClientId')!;
  }

  public get sofProxyOutClientSecret() {
    return this.formE.get('sofProxyOutClientSecret')!;
  }

  public get sofProxyOutScope() {
    return this.formE.get('sofProxyOutScope')!;
  }

  public get sofProxySqlInjection() {
    return this.formE.get('sofProxySqlInjection')!;
  }

  public get sofProxyTraffic() {
    return this.formE.get('sofProxyTraffic')!;
  }

  public get sofProxyXss() {
    return this.formE.get('sofProxyXss')!;
  }

  public get sofProxyXxe() {
    return this.formE.get('sofProxyXxe')!;
  }

  public get sofProxyTps() {
    return this.formE.get('sofProxyTps')!;
  }

  public get sofProxyIgnoreApi() {
    return this.formE.get('sofProxyIgnoreApi')!;
  }

  public get sofProxyClientId() {
    return this.formE.get('sofProxyClientId')!;
  }

  public get sofProxyShowLog() {
    return this.formE.get('sofProxyShowLog')!;
  }

  public get sofProxySticky() {
    return this.formE.get('sofProxySticky')!;
  }

  public get diversionList() {
    return this.formE.get('diversionList')!;
  }

  public get createDateTime() {
    return this.formE.get('createDateTime')!;
  }

  public get createUser() {
    return this.formE.get('createUser')!;
  }

  public get updateDateTime() {
    return this.formE.get('updateDateTime')!;
  }

  public get updateUser() {
    return this.formE.get('updateUser')!;
  }

  public get version() {
    return this.formE.get('version')!;
  }

  //   public get file() {
  //   return this.formFile.get('file')!;
  // }
  // public get fileName() {
  //   return this.formFile.get('fileName')!;
  // }
}

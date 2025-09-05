import { AA0313KeyVal } from './../../../models/api/ApiService/aa0313.interface';
import { map, filter, pairwise } from 'rxjs/operators';
import { ApiTestComponent } from './../ac0316/api-test/api-test.component';
import { Ac0316Component } from './../ac0316/ac0316.component';
import { DialogService } from 'primeng/dynamicdialog';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0142Req } from './../../../models/api/ServerService/dpb0142.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
// import { ApiService } from 'src/app/shared/services/api-api.service';
import { Router, ActivatedRoute } from '@angular/router';
import { BaseComponent } from 'src/app/layout/base-component';
import {
  Component,
  OnInit,
  ViewChild,
  AfterViewInit,
  ChangeDetectorRef,
} from '@angular/core';
import { FormBuilder, FormGroup, FormArray, FormControl } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { MessageService, ConfirmationService, MenuItem } from 'primeng/api';
import {
  AA0303Req,
  AA0303Item,
} from 'src/app/models/api/ApiService/aa0303.interface';
import { APIStatusPipe } from 'src/app/shared/pipes/api-status.pipe';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { APISrcPipe } from 'src/app/shared/pipes/api-src.pipe';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { TranslateService } from '@ngx-translate/core';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import {
  AA0301Item,
  AA0301Req,
} from 'src/app/models/api/ApiService/aa0301_v3.interfcae';
import {
  AA0302RedirectByIpData,
  AA0302Req,
  AA0302Resp,
} from 'src/app/models/api/ApiService/aa0302_v3.interface';
import {
  AA0320Item,
  AA0320Req,
} from 'src/app/models/api/ApiService/aa0320.interface';
// import { CommonAPI } from 'src/app/shared/register-api/common-api.class';
// import { RegHostService } from 'src/app/shared/services/api-reg-host.service';
import { AA0806Req } from 'src/app/models/api/RegHostService/aa0806.interface';
import { AA0304Req } from 'src/app/models/api/ApiService/aa0304.interface';
import { AA0313Req } from 'src/app/models/api/ApiService/aa0313.interface';
import { AA0511Req } from 'src/app/models/api/UtilService/aa0511_v3.interface';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { AA0317Req } from 'src/app/models/api/ApiService/aa0317.interface';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { RegHostService } from 'src/app/shared/services/api-reg-host.service';
import { CommonAPI } from 'src/app/shared/register-api/common-api.class';
import { environment } from 'src/environments/environment';
import { SwaggerComponent } from './swagger/swagger.component';
import * as base64 from 'js-base64';
import { LabelListComponent } from 'src/app/shared/label-list/label-list.component';
import {
  AA0428Req,
  ReqAA0428,
} from 'src/app/models/api/ApiService/aa0428.interfcae';
import { ApiStatusModifyComponent } from './api-status-modify/api-status-modify.component';
import { MultiSelect, MultiSelectModule } from 'primeng/multiselect';
import {
  AA0318Item,
  AA0318Req,
} from 'src/app/models/api/ApiService/aa0318.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import {
  AA0319Req,
  AA0319ReqItem,
} from 'src/app/models/api/ApiService/aa0319.interface';
import { ToolServiceWebhoook } from 'src/app/shared/services/tool-webhook.service';
import { NotifyListComponent } from 'src/app/shared/notify-list/notify-list.component';
import { JwtSettingComponent } from './jwt-setting/jwt-setting.component';
import { AA0434DTO } from 'src/app/models/api/ApiService/aa0434.interfcae';
import { TOrgService } from 'src/app/shared/services/org.service';
import { OrganizationComponent } from 'src/app/shared/organization/organization.component';
import { Subscription } from 'rxjs';
import * as ValidatorFns from '../../../shared/validator-functions';

@Component({
  selector: 'app-ac0301',
  templateUrl: './ac0301.component.html',
  styleUrls: ['./ac0301.component.css'],
  providers: [
    ApiService,
    MessageService,
    APIStatusPipe,
    APISrcPipe,
    RegHostService,
    ConfirmationService,
    MultiSelect,
  ],
})
export class Ac0301Component extends BaseComponent implements OnInit {
  @ViewChild('dialog') _dialog!: DialogComponent;
  @ViewChild('labelFilter')
  lableFilter!: MultiSelect;
  form!: FormGroup;
  queryForm: FormGroup;
  dialogTitle: string = '';
  formOperate = FormOperate;
  selected: Array<AA0301Item> = new Array<AA0301Item>();
  cols: (
    | { field: string; header: string; type?: undefined }
    | { field: string; header: string; type: APIStatusPipe }
  )[] = [];
  rowcount: number = 0;
  canDetail: boolean = false;
  canStatusUpdate: boolean = false;
  canUpdate: boolean = false;
  canDelete: boolean = false;
  canImport: boolean = false;
  canSetting: boolean = false;
  canAPITest: boolean = false;
  acConf: AA0510Resp;
  queryJwtSettingFlags: { label: string; value: string }[] = [];
  updateJwtSettingFlags: { label: string; value: string }[] = [];
  pageNum: number = 1; // 1: 查詢，2: 詳細資料、3: 更新API
  currentTitle: string = this.title;
  apiSrcOpt: { label: string; value: string }[] = [];
  apiStatusOpt: { label: string; value: string }[] = [];
  publicFlags: { label: string; value: string }[] = [];
  dataList: Array<AA0301Item> = [];
  direction: string = 'asc';
  lastRowdata?: AA0301Item;
  apiDetail?: AA0302Resp;
  tokenPayloadFlag: boolean = false;
  detailForm: FormGroup;
  detailCols: { field: string; header: string }[] = [];
  apiGroupList: Array<AA0320Item> = [];
  apiGroupListRowcount: number = 0;
  updateForm: FormGroup;
  protocols: { label: string; value: string }[] = [];
  methodOfJsons: { label: string; value: string }[] = [];
  dataFormats: { label: string; value: string }[] = [];
  // reghostIds: { label: string; value: string }[] = [];
  currentUpdateStatusAction: string = '';

  initData: { [key: string]: string } = {};
  apiCacheFlags: { label: string; value: string }[] = [];

  srcUrlPool: { percent: string; url: string }[] = [];
  ipSrcUrl: {
    ipForRediret: string;
    srcUrlPool: { percent: string; url: string }[];
  }[] = [];

  searchByLabel: boolean = false;
  selLabelList: Array<string> = [];
  showLabelList_tip: boolean = false;
  lblList: { label: string; value: string }[] = [];
  apiFile?: File;

  import_apiList: Array<AA0318Item> = [];
  import_cols: { field: string }[] = [];
  import_selected: Array<AA0318Item> = [];
  fileBatchNo: number = 0;

  viewModeList: { [key: number]: Array<string> } = {
    0: [
      'apiDesc',
      'moduleName',
      'apiKey',
      'apiSrc',
      'JWTnLabel',
      'enableScheduledDate',
      'disableScheduledDate',
    ],
    1: [
      'apiKey',
      'org',
      'createUser',
      'createDate',
      'updateUser',
      'updateDate',
    ],
    2: ['apiKey', 'apiCacheFlag', 'methodOfJson', 'noAuth'],
  };

  // dgrProtocol:Array<string> = [];

  isWebhook: boolean = false;
  isAiGateway: boolean = false;
  reloadFlag: boolean = false;

  jwtSettingSub?: Subscription;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private router: Router,
    private fb: FormBuilder,
    private apiService: ApiService,
    private messageService: MessageService,
    private stat: APIStatusPipe,
    private tool: ToolService,
    private alert: AlertService,
    private translate: TranslateService,
    private ngxService: NgxUiLoaderService,
    private list: ListService,
    private roleService: RoleService,
    private regService: RegHostService,
    private serverService: ServerService,
    private confirmationService: ConfirmationService,
    private dialogService: DialogService, // private utilService: UtilService
    private fileService: FileService,
    private cdRef: ChangeDetectorRef,
    private toolServiceW: ToolServiceWebhoook,
    private orgService: TOrgService
  ) {
    super(route, tr);

    this.queryForm = this.fb.group({
      apiKey: new FormControl(''),
      apiName: new FormControl(''),
      moduleName: new FormControl(''),
      apiStatus: new FormControl('null'),
      apiSrc: new FormArray([]),
      jwtSetting: new FormControl(false),
      jweFlag: new FormControl({ value: '0', disabled: true }),
      jweFlagResp: new FormControl({ value: '0', disabled: true }),
      keyword: new FormControl(''),
      publicFlag: new FormControl('0'),
      queryJweFlag: new FormControl('null'),
      queryJweFlagResp: new FormControl('null'),
    });

    this.detailForm = this.fb.group({
      keyword: new FormControl(''),
    });

    this.updateForm = this.fb.group({
      apiKey: new FormControl(''),
      moduleName: new FormControl(''),
      apiName: new FormControl(''),
      apiStatus: new FormControl(false),
      jwtSetting: new FormControl(false),
      jweFlag: new FormControl({ value: '0', disabled: true }),
      jweFlagResp: new FormControl({ value: '0', disabled: true }),
      protocol: new FormControl(''),
      srcUrl: new FormControl(''),
      urlRID: new FormControl(false),
      noOAuth: new FormControl(false),
      tokenPayload: new FormControl(false),
      methodOfJson: new FormControl(''),
      dataFormat: new FormControl(null),
      reghostId: new FormControl(null),
      apiDesc: new FormControl(''),
      apiCacheFlag: new FormControl(''),
      mockStatusCode: new FormControl(null),
      mockHeaders: new FormControl(null),
      mockBody: new FormControl(null),

      redirectByIp: new FormControl(false),
      redirectByIpDataList: new FormControl([]),

      headerMaskPolicy: new FormControl(0), //; 0,1,2,3
      headerMaskPolicyNum: new FormControl(1), //; 1~9999
      headerMaskPolicySymbol: new FormControl('*'), //;   1 ~10
      headerMaskKey: new FormControl(''), //;  XXXX,XXXXXX,XXXXX

      bodyMaskPolicy: new FormControl(0), //; 0,1,2,3
      bodyMaskPolicyNum: new FormControl(1), //; 1~9999
      bodyMaskPolicySymbol: new FormControl('*'), //;   1 ~10
      bodyMaskKeyword: new FormControl(''), //;  XXXX,XXXXXX,XXXX

      labelList: new FormControl([]),
      fixedCacheTime: new FormControl(0),
      failDiscoveryPolicy: new FormControl(''),
      failHandlePolicy: new FormControl(''),
      notifyNameList: new FormControl(),
      isCorsAllowOrigin: new FormControl(),
      isCorsAllowMethods: new FormControl(),
      isCorsAllowHeaders: new FormControl(),
      corsAllowOrigin: new FormControl(),
      corsAllowMethods: new FormControl(),
      corsAllowHeaders: new FormControl(),
    });

    //取得acConf
    this.acConf = this.tool.getAcConf();
    this.loadMenu();
  }

  ngOnInit() {
    this.form = this.fb.group({
      file: new FormControl(),
      fileName: new FormControl({ value: '', disabled: true }),
      fileSize: new FormControl(''),
    });
    this.route.params.subscribe((res: any) => {
      if (res && res.apiSrc) {
        let request = JSON.parse(res.request);
        this.queryForm.get('apiStatus')!.setValue('');
        this.queryForm.get('apiSrc')!.setValue('');
        this.queryForm.get('apiKey')!.setValue(request.apiKey);
        this.queryForm.get('moduleName')!.setValue(request.moduleName);
        this.submitForm();
      }
    });
    // API來源
    let apiSrcReqBody = {
      encodeItemNo:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('API_SRC')) + ',' + 33,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(apiSrcReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _apiSrcOpt: { label: string; value: string }[] = [];
        if (res.RespBody?.subItems) {
          for (let item of res.RespBody?.subItems) {
            if (this.acConf.net == 'false' && item.subitemNo == 'N') {
              continue;
            }
            if (
              this.acConf.edition == 'Express' &&
              (item.subitemNo == 'M' ||
                item.subitemNo == 'N' ||
                item.subitemNo == 'C')
            ) {
              continue;
            }
            _apiSrcOpt.push({
              label: item.subitemName,
              value: item!.subitemNo,
            });
          }
        }
        this.apiSrcOpt = _apiSrcOpt;
        const arrayApiSrc = <FormArray>this.queryForm.get('apiSrc');
        for (let item of this.apiSrcOpt) {
          if (this.acConf.edition == 'Express') {
            arrayApiSrc.push(
              this.fb.group({
                name: item.label,
                value: item.value,
                selected: true,
              })
            );
            this.queryForm.get('apiSrc')!.disable();
          } else {
            arrayApiSrc.push(
              this.fb.group({
                name: item.label,
                value: item.value,
                selected: false,
              })
            );
          }
        }
      }
    });
    // API狀態
    let apiStatusReqBody = {
      encodeItemNo:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('ENABLE_FLAG')) +
        ',' +
        9,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(apiStatusReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _apiStatusOpt: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map((item) => {
            if (item.subitemNo != '2') {
              // 不要鎖定
              _apiStatusOpt.push({
                label: item.subitemName,
                value: item.param1 ? item.param1 : 'null',
              });
            }
          });
        }
        this.apiStatusOpt = _apiStatusOpt;
      }
    });
    // 開放狀態
    let publicFlagReqBody = {
      encodeItemNo:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('API_AUTHORITY')) +
        ',' +
        7,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(publicFlagReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _publicFlags: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map((item) => {
            if (item.subitemNo != '-1') {
              _publicFlags.push({
                label: item.subitemName,
                value: item.subitemNo,
              });
            }
          });
        }
        this.publicFlags = _publicFlags;
      }
    });

    // Api Cache Flag
    let apiCacheReqBody = {
      encodeItemNo:
        // this.tool.Base64Encoder(this.tool.BcryptEncoder('API_AUTHORITY')) +
        this.tool.Base64Encoder(this.tool.BcryptEncoder('API_CACHE_FLAG')) +
        ',' +
        57,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(apiCacheReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let tmp: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map((item) => {
            if (item.subitemNo != '-1') {
              tmp.push({
                label: item.subitemName,
                value: item.subitemNo,
              });
            }
          });
        }
        this.apiCacheFlags = tmp;
      }
    });
    // JWT設定
    let jwtSettingReqBody = {
      encodeItemNo:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('API_JWT_FLAG')) +
        ',' +
        31,
      isDefault: 'N',
    } as DPB0047Req;
    this.list
      .querySubItemsByItemNo(jwtSettingReqBody)
      .subscribe(async (res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          const code = ['all'];
          const dict = await this.tool.getDict(code);
          let _queryJwtSettingFlags = [{ label: dict['all'], value: 'null' }];
          let _updateJwtSettingFlags: { label: string; value: string }[] = [];
          if (res.RespBody.subItems) {
            res.RespBody.subItems.map((item) => {
              _queryJwtSettingFlags.push({
                label: item.subitemName,
                value: item.subitemNo,
              });
              _updateJwtSettingFlags.push({
                label: item.subitemName,
                value: item.subitemNo,
              });
            });
          }
          this.queryJwtSettingFlags = _queryJwtSettingFlags;
          this.updateJwtSettingFlags = _updateJwtSettingFlags;
        }
      });
    // 權限
    this.roleService
      .queryRTMapByUk({
        txIdList: ['AA0302', 'AA0303', 'AA0304', 'AA0305', 'AA0318', 'AA0312'],
      })
      .subscribe((res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.canDetail = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0302'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0302')!
                .available
            : false;
          this.canStatusUpdate = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0303'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0303')!
                .available
            : false;
          this.canUpdate = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0304'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0304')!
                .available
            : false;
          this.canDelete = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0305'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0305')!
                .available
            : false;
          this.canImport = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0318'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0318')!
                .available
            : false;
          this.canSetting =
            this.tool.getAcConf().edition === 'Enterprise' ? true : false;

          this.canAPITest = res.RespBody.dataList.find(
            (item) => item.txId === 'AA0312'
          )
            ? res.RespBody.dataList.find((item) => item.txId === 'AA0312')!
                .available
            : false;
        }
      });
    // protocol
    this.protocols = [
      { label: 'https://', value: 'https' },
      { label: 'http://', value: 'http' },
    ];
    // Http method
    this.methodOfJsons = CommonAPI.methods;
    // 資料格式
    let dataFormatReqBody = {
      encodeItemNo:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('API_DATA_FORMAT')) +
        ',' +
        30,
      isDefault: 'N',
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(dataFormatReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _dataFormats: { label: string; value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map((item) => {
            _dataFormats.push({
              label: item.subitemName,
              value: item.subitemNo,
            });
          });
        }
        this.dataFormats = _dataFormats;
      }
    });
    this.init();

    this.headerMaskPolicy.valueChanges
      .pipe(pairwise())
      .subscribe(([prev, next]) => {
        if (!['1', '2', '3'].find((item) => item === prev)) {
          this.headerMaskPolicyNum.setValue(1);
          this.headerMaskPolicySymbol.setValue('*');
        }
      });
    this.bodyMaskPolicy.valueChanges
      .pipe(pairwise())
      .subscribe(([prev, next]) => {
        if (!['1', '2', '3', '4'].find((item) => item === prev)) {
          this.bodyMaskPolicyNum.setValue(1);
          this.bodyMaskPolicySymbol.setValue('*');
        }
      });

    this.labelList.valueChanges.subscribe((res) => {
      this.labelList.setValue(
        Array.isArray(res) ? res.map((item) => item.toLowerCase()) : [],
        { emitEvent: false }
      );
    });

    this.apiCacheFlag.valueChanges.subscribe((res) => {
      if (res === '3') {
        this.fixedCacheTime.setValue(this.apiDetail?.fixedCacheTime);
        this.fixedCacheTime.enable();
      } else {
        this.fixedCacheTime.disable();
      }
    });

    this.fixedCacheTime.valueChanges.subscribe((res) => {
      if (res && parseInt(res) >= 0) {
        if (parseInt(res) > 999999) {
          this.fixedCacheTime.setValue('999999', { emitEvent: false });
        } else {
          this.fixedCacheTime.setValue(parseInt(res).toString(), {
            emitEvent: false,
          });
        }
      } else {
        this.fixedCacheTime.setValue('0', { emitEvent: false });
      }
    });

    this.queryAllLabel();
  }

  queryAllLabel() {
    //標籤資料清單
    this.apiService.queryAllLabel_ignore1298().subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.lblList = res.RespBody.labelList.map((item) => {
          return { label: item, value: item };
        });
      } else this.lblList = [];
    });
  }

  public async init(): Promise<void> {
    const dict = await this.tool.getDict([
      'status',
      'api_key',
      'api_src',
      'module_name',
      'api_name',
      'jwt_setting',
      'api_desc',
      'recent_update_date',
      'group_id',
      'group_name',
      'group_alias',
      'group_desc',
      'label_tag',
      'apiStatus.scheduledLaunchDate',
      'apiStatus.scheduledDiscontinuationDate',
    ]);
    this.cols = [
      { field: 'apiStatus', header: dict['status'], type: this.stat },
      { field: 'apiName', header: dict['api_name'] },
      { field: 'apiDesc', header: dict['api_desc'] },
      { field: 'moduleName', header: dict['module_name'] },
      { field: 'apiKey', header: dict['api_key'] },
      { field: 'apiSrc', header: dict['api_src'] },
      {
        field: 'jweFlag',
        header: `${dict['jwt_setting']}/${dict['label_tag']}`,
      },
      {
        field: 'enableScheduledDate',
        header: dict['apiStatus.scheduledLaunchDate'],
      },
      // {
      //   field: 'disableScheduledDate',
      //   header: dict['apiStatus.scheduledDiscontinuationDate'],
      // },
      { field: 'updateTime', header: dict['recent_update_date'] },
    ];
    this.detailCols = [
      { field: 'gId', header: dict['group_id'] },
      {
        field: 'name',
        header: `${dict['group_name']}(${dict['group_alias']})`,
      },
      { field: 'desc', header: dict['group_desc'] },
    ];
    this.import_cols = [
      { field: 'apiKey' },
      { field: 'moduleName' },
      { field: 'apiName' },
      { field: 'apiSrc' },
      { field: 'endpoint' },
      { field: 'checkAct' },
      { field: 'result' },
    ];
    // 預設查詢
    let ReqBody = {
      keyword: this.queryKeyword!.value,
      apiSrc: this.acConf.edition == 'Express' ? ['R'] : [],
      publicFlag:
        this.tool.Base64Encoder(this.tool.BcryptEncoder('0')) + ',' + 0,
      paging: 'Y',
    } as AA0301Req;
    this.apiService.queryAPIList_v3_ignore1298(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
        this.lastRowdata = this.dataList[this.dataList.length - 1];
      }
      this.searchByLabel = false;
    });
  }

  submitForm() {
    this.selected = [];
    this.dataList = [];
    this.rowcount = this.dataList.length;
    let apisrc = new Array();
    const arrayApiSrc = <FormArray>this.queryForm.get('apiSrc');
    for (let i = 0; i < arrayApiSrc.controls.length; i++) {
      var src = arrayApiSrc.controls[i];
      if (src.get('selected')!.value == false) {
        continue;
      }
      apisrc.push(src.get('value')!.value);
    }
    let ReqBody = {
      keyword: this.queryKeyword!.value,
      apiSrc: apisrc,
      publicFlag:
        this.tool.Base64Encoder(
          this.tool.BcryptEncoder(this.publicFlag!.value)
        ) +
        ',' +
        this.publicFlags.findIndex(
          (item) => item.value == this.publicFlag!.value
        ),
      paging: 'Y',
    } as AA0301Req;
    if (this.apiStatus!.value != 'null') {
      ReqBody.apiStatus =
        this.tool.Base64Encoder(
          this.tool.BcryptEncoder(this.apiStatus!.value)
        ) +
        ',' +
        this.apiStatusOpt.findIndex(
          (item) => item.value == this.apiStatus!.value
        );
    }
    if (this.q_queryJweFlag!.value != 'null') {
      ReqBody.jweFlag =
        this.tool.Base64Encoder(
          this.tool.BcryptEncoder(this.q_queryJweFlag!.value)
        ) +
        ',' +
        (this.queryJwtSettingFlags.findIndex(
          (item) => item.value == this.q_queryJweFlag!.value
        ) -
          1);
    }
    if (this.q_queryJweFlagResp!.value != 'null') {
      ReqBody.jweFlagResp =
        this.tool.Base64Encoder(
          this.tool.BcryptEncoder(this.q_queryJweFlagResp!.value)
        ) +
        ',' +
        (this.queryJwtSettingFlags.findIndex(
          (item) => item.value == this.q_queryJweFlagResp!.value
        ) -
          1);
    }
    this.apiService.queryAPIList_v3(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
        this.lastRowdata = this.dataList[this.dataList.length - 1];
      }
      this.searchByLabel = false;
    });
  }

  moreApiListData() {
    if (this.searchByLabel) {
      let req = {
        labelList: this.selLabelList,
        moduleName: this.dataList[this.dataList.length - 1].moduleName.t
          ? this.dataList[this.dataList.length - 1].moduleName.ori
          : this.dataList[this.dataList.length - 1].moduleName.val,
        apiKey: this.dataList[this.dataList.length - 1].apiKey.t
          ? this.dataList[this.dataList.length - 1].apiKey.ori
          : this.dataList[this.dataList.length - 1].apiKey.val,
        paging: 'Y',
      } as AA0428Req;
      this.apiService.queryAPIListByLabel(req).subscribe((res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.dataList = this.dataList.concat(res.RespBody.dataList);
          this.rowcount = this.dataList.length;
          this.lastRowdata = this.dataList[this.dataList.length - 1];
        }
        this.searchByLabel = true;
      });
    } else {
      let apisrc = new Array();
      const arrayApiSrc = <FormArray>this.queryForm.get('apiSrc');
      for (let i = 0; i < arrayApiSrc.controls.length; i++) {
        var src = arrayApiSrc.controls[i];
        if (src.get('selected')!.value == false) {
          continue;
        }
        apisrc.push(src.get('value')!.value);
      }
      let ReqBody = {
        moduleName:
          this.lastRowdata!.moduleName.t == true
            ? this.lastRowdata!.moduleName.ori
            : this.lastRowdata!.moduleName.val,
        apiKey:
          this.lastRowdata!.apiKey.t == true
            ? this.lastRowdata!.apiKey.ori
            : this.lastRowdata!.apiKey.val,
        keyword: this.queryKeyword!.value,
        apiSrc: apisrc,
        publicFlag:
          this.tool.Base64Encoder(
            this.tool.BcryptEncoder(this.publicFlag!.value)
          ) +
          ',' +
          this.publicFlags.findIndex(
            (item) => item.value == this.publicFlag!.value
          ),
        paging: 'Y',
      } as AA0301Req;
      if (this.apiStatus!.value != 'null') {
        ReqBody.apiStatus =
          this.tool.Base64Encoder(
            this.tool.BcryptEncoder(this.apiStatus!.value)
          ) +
          ',' +
          this.apiStatusOpt.findIndex(
            (item) => item.value == this.apiStatus!.value
          );
      }
      if (this.q_queryJweFlag!.value != 'null') {
        ReqBody.jweFlag =
          this.tool.Base64Encoder(
            this.tool.BcryptEncoder(this.q_queryJweFlag!.value)
          ) +
          ',' +
          (this.queryJwtSettingFlags.findIndex(
            (item) => item.value == this.q_queryJweFlag!.value
          ) -
            1);
      }
      if (this.q_queryJweFlagResp!.value != 'null') {
        ReqBody.jweFlagResp =
          this.tool.Base64Encoder(
            this.tool.BcryptEncoder(this.q_queryJweFlagResp!.value)
          ) +
          ',' +
          (this.queryJwtSettingFlags.findIndex(
            (item) => item.value == this.q_queryJweFlagResp!.value
          ) -
            1);
      }
      this.apiService.queryAPIList_v3(ReqBody).subscribe((res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.dataList = this.dataList.concat(res.RespBody.dataList);
          this.rowcount = this.dataList.length;
          this.lastRowdata = this.dataList[this.dataList.length - 1];
        }
        this.searchByLabel = false;
      });
    }
  }

  modifyApiStatus(rowData: AA0301Item, field: string) {
    if (field == 'enableScheduledDate') {
      if (rowData.apiStatus.v == '1' && rowData[field] == 0) {
        this.translate.get('apiStatus.enabled').subscribe((dict) => {
          this.alert.ok(dict, '');
        });
        return;
      }
      this.selected = [rowData];
      if (rowData.enableScheduledDate == 0) {
        this.updateApiStatus('1');
      } else {
        this.updateApiStatus('c', 'revokeEnable');
      }
    } else if (field == 'disableScheduledDate') {
      if (rowData.apiStatus.v == '2' && rowData[field] == 0) {
        this.translate.get('apiStatus.disabled').subscribe((dict) => {
          this.alert.ok(dict, '');
        });
        return;
      }
      this.selected = [rowData];
      if (rowData.disableScheduledDate == 0) {
        this.updateApiStatus('2');
      } else {
        this.updateApiStatus('c', 'revokeDisable');
      }
    }
  }

  async updateApiStatus(action: string, revokeFlag?: string) {
    if (!this.dataList) return;
    if (!this.selected && !this.selected['length']) return;
    this.currentUpdateStatusAction = action;
    const code = [
      'button.enable',
      'button.disable',
      'button.delete',
      'button.export',
      'cfm_export_rc_api',
    ];
    const dict = await this.tool.getDict(code);
    let statusText = {
      0: dict['button.delete'],
      1: dict['button.enable'],
      2: dict['button.disable'],
      4: dict['button.export'],
    };
    switch (this.currentUpdateStatusAction) {
      case '0': {
        this.translate.get('alert').subscribe((alert) => {
          this.translate
            .get('alert.text.actions', { statusText: statusText[action] })
            .subscribe((action) => {
              // this.messageService.add({ key: 'confirm', sticky: true, severity: 'warn', summary: action, });

              this.confirmationService.confirm({
                key: 'cd',
                header: statusText[this.currentUpdateStatusAction],
                message: action,
                accept: () => {
                  this.active();
                },
              });
            });
        });
        break;
      }
      case '1':
      case '2':
        const code = [
          'apiStatus.scheduledLaunchDate',
          'apiStatus.scheduledDiscontinuationDate',
          'apiStatus.cancelScheduledDate',
        ];
        const dict = await this.tool.getDict(code);
        const ref = this.dialogService.open(ApiStatusModifyComponent, {
          header:
            this.currentUpdateStatusAction == '1'
              ? dict['apiStatus.scheduledLaunchDate']
              : dict['apiStatus.scheduledDiscontinuationDate'],
          width: '400px',
          data: {
            status: this.currentUpdateStatusAction,
          },
        });
        ref.onClose.subscribe((res) => {
          if (res) {
            if (!res.modifyDate || res.modifyDate == '') {
              this.active(0, res.apiStatus);
            } else {
              this.active(new Date(res.modifyDate).getTime(), res.apiStatus);
            }
          }
        });
        break;
      case 'c':
        const codeC = [
          'apiStatus.cancelScheduledDate',
          'message.update',
          'api_status',
          'message.update',
          'message.success',
        ];
        const dictC = await this.tool.getDict(codeC);
        const ref2 = this.dialogService.open(ApiStatusModifyComponent, {
          header: dictC['apiStatus.cancelScheduledDate'],
          width: '600px',
          data: {
            status: this.currentUpdateStatusAction,
            revokeFlag: revokeFlag ? revokeFlag : 'revokeAll',
          },
        });
        ref2.onClose.subscribe((res) => {
          if (res) {
            // 清除 API 預定啟用日期或停用日期
            this.apiService
              .cancelScheduledDate({
                apiList: this.selected.map((item) => {
                  return {
                    moduleName: item.moduleName.ori
                      ? item.moduleName.ori
                      : item.moduleName.val,

                    apiKey: item.apiKey.ori ? item.apiKey.ori : item.apiKey.val,
                  };
                }),
                revokeFlag: res.revokeFlag,
              })
              .subscribe((res) => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dictC['message.update']} ${dictC['api_status']}`,
                    detail: `${dictC['message.update']} ${dictC['message.success']}`,
                  });
                  this.searchByLabel
                    ? this.queryAPIListByLabel(this.selLabelList)
                    : this.submitForm();
                }
              });
          }
        });
        break;
      case '4':
        this.translate.get('alert').subscribe((alert) => {
          this.translate
            .get('cfm_export_rc_api', { count: this.selected.length })
            .subscribe((i18n) => {
              // this.messageService.add({ key: 'confirm', sticky: true, severity: 'warn', summary: i18n });

              this.confirmationService.confirm({
                key: 'cd',
                header: statusText[this.currentUpdateStatusAction],
                message: i18n,
                accept: () => {
                  this.active();
                },
              });
            });
        });
        break;
    }
  }

  async active(scheduledDate?: number, apiStatus?: string) {
    this.messageService.clear('confirm');
    const code = [
      'message.update',
      'message.success',
      'api_status',
      'message.delete',
    ];
    const dict = await this.tool.getDict(code);
    let _apiList = this.selected.map((item) => {
      return {
        moduleName: item.moduleName.ori
          ? item.moduleName.ori
          : item.moduleName.val,
        // item.moduleName.t
        //   ? item.moduleName.ori
        //   : item.moduleName.val,
        apiKey: item.apiKey.ori ? item.apiKey.ori : item.apiKey.val,
        // item.apiKey.t ? item.apiKey.ori : item.apiKey.val,
      } as AA0303Item;
    });
    switch (this.currentUpdateStatusAction) {
      case '0': // 刪除API
        let deleteReqBody = {
          ignoreAlert: 'N',
          apiList: _apiList,
          apiStatus: this.currentUpdateStatusAction,
        } as AA0303Req;
        this.apiService.updateAPIStatus_1(deleteReqBody).subscribe((res) => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            if (res.RespBody.msg) {
              // this.messageService.add({ key: 'delete', sticky: true, severity: 'warn', summary: res.RespBody.msg });

              this.confirmationService.confirm({
                key: 'del',
                header: ' ',
                message: res.RespBody.msg,
                accept: () => {
                  this.defineDelete();
                },
              });
            } else {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.delete']} API`,
                detail: `${dict['message.delete']} ${dict['message.success']}`,
              });
              this.queryAllLabel();
              this.searchByLabel
                ? this.queryAPIListByLabel(this.selLabelList)
                : this.submitForm();
            }
          }
        });
        break;
      case '1': // 啟用API
      case '2': // 停用API
        let ReqBody = {
          ignoreAlert: 'Y',
          apiList: _apiList,
          apiStatus: apiStatus ? apiStatus : this.currentUpdateStatusAction,
        } as AA0303Req;
        if (scheduledDate) {
          ReqBody.scheduledDate = scheduledDate;
        }
        this.apiService.updateAPIStatus_1(ReqBody).subscribe((res) => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} ${dict['api_status']}`,
              detail: `${dict['message.update']} ${dict['message.success']}`,
            });
            this.searchByLabel
              ? this.queryAPIListByLabel(this.selLabelList)
              : this.submitForm();
          }
        });
        break;

      case '4': // export api list
        this.ngxService.start();
        let exportReqBody = {
          apiList: _apiList,
        } as AA0317Req;
        this.apiService.exportRegCompAPIs(exportReqBody).subscribe((res) => {
          this.ngxService.stop();
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            let fileName = res.RespBody.fileName;
            let jsonString = JSON.stringify(res.RespBody.data);
            let blob = new Blob([jsonString], { type: 'application/json' });
            let file = new File([blob], fileName);
            const url = window.URL.createObjectURL(file);
            const a = document.createElement('a');
            document.body.appendChild(a);
            a.setAttribute('style', 'display: none');
            a.href = url;
            a.download = fileName;
            a.click();
            window.URL.revokeObjectURL(url);
            a.remove();
          }
        });
        break;
      default:
        break;
    }
  }

  jwtSettingChange() {
    if (!this.dataList) return;
    if (!this.selected && !this.selected['length']) return;
    let _apiList = this.selected.map((item) => {
      return {
        moduleName: item.moduleName.t
          ? item.moduleName.ori
          : item.moduleName.val,
        apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val,
      } as AA0303Item;
    });
    let ReqBody = {
      ignoreAlert: 'Y',
      apiList: _apiList,
      jweFlag: this.q_jweFlag!.value,
      jweFlagResp: this.q_jweFlagResp!.value,
    } as AA0303Req;
    this.apiService.updateAPIStatus_1(ReqBody).subscribe(async (r) => {
      this.ngxService.stop();
      if (this.tool.checkDpSuccess(r.ResHeader)) {
        const code = ['message.update', 'jwt_setting', 'message.success'];
        const dict = await this.tool.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.update']} ${dict['jwt_setting']}`,
          detail: `${dict['message.update']} ${dict['message.success']}`,
        });
        let _selectdApi = this.selected.map((item) => {
          return {
            moduleName: item.moduleName,
            apiKey: item.apiKey,
            apiStatus: item.apiStatus,
            apiSrc: item.apiSrc,
            apiName: item.apiName,
            apiDesc: item.apiDesc,
            jweFlag: item.jweFlag,
            jweFlagResp: item.jweFlagResp,
            updateTime: item.updateTime,
            org: item.org,
          } as AA0301Item;
        });
        let apisrc = new Array();
        const arrayApiSrc = <FormArray>this.queryForm.get('apiSrc');
        for (let i = 0; i < arrayApiSrc.controls.length; i++) {
          var src = arrayApiSrc.controls[i];
          if (src.get('selected')!.value == false) {
            continue;
          }
          apisrc.push(src.get('value')!.value);
        }
        let ReqBody = {
          keyword: this.queryKeyword!.value,
          apiSrc: apisrc,
          publicFlag:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.publicFlag!.value)
            ) +
            ',' +
            this.publicFlags.findIndex(
              (item) => item.value == this.publicFlag!.value
            ),
          paging: 'Y',
        } as AA0301Req;
        if (this.apiStatus!.value != 'null') {
          ReqBody.apiStatus =
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.apiStatus!.value)
            ) +
            ',' +
            this.apiStatusOpt.findIndex(
              (item) => item.value == this.apiStatus!.value
            );
        }
        if (this.q_queryJweFlag!.value != 'null') {
          ReqBody.jweFlag =
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.q_jweFlag!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.q_jweFlag!.value
            );
        }
        if (this.q_queryJweFlagResp!.value != 'null') {
          ReqBody.jweFlagResp =
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.q_jweFlagResp!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.q_jweFlagResp!.value
            );
        }
        this.apiService.queryAPIList_v3(ReqBody).subscribe((res) => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.selected = [];
            this.dataList = res.RespBody.dataList;
            this.rowcount = this.dataList.length;
            _selectdApi.map((item) => {
              let tmp = this.dataList.find(
                (d) =>
                  (d.apiKey.t ? d.apiKey.ori : d.apiKey.val) ==
                    (item.apiKey.t ? item.apiKey.ori : item.apiKey.val) &&
                  (d.moduleName.t ? d.moduleName.ori : d.moduleName.val) ==
                    (item.moduleName.t
                      ? item.moduleName.ori
                      : item.moduleName.val)
              );
              if (tmp) this.selected.push(tmp);
            });
          }
          this.searchByLabel = false;
        });
      }
    });
  }

  defineDelete() {
    this.messageService.clear('delete');
    let _apiList = this.selected.map((item) => {
      return {
        moduleName: item.moduleName.t
          ? item.moduleName.ori
          : item.moduleName.val,
        apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val,
      } as AA0303Item;
    });
    let ReqBody = {
      ignoreAlert: 'Y',
      apiList: _apiList,
      apiStatus: this.currentUpdateStatusAction,
    } as AA0303Req;
    this.apiService.updateAPIStatus_1(ReqBody).subscribe(async (res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['message.success', 'message.delete'];
        const dict = await this.tool.getDict(code);
        this.messageService.add({
          severity: 'success',
          summary: `${dict['message.delete']} API`,
          detail: `${dict['message.delete']} ${dict['message.success']}`,
        });
        this.queryAllLabel();
        this.searchByLabel
          ? this.queryAPIListByLabel(this.selLabelList)
          : this.submitForm();
      }
    });
  }

  onReject() {
    this.messageService.clear();
  }

  async showDialog(
    rowData: AA0301Item,
    operation: FormOperate,
    type: string = ''
  ) {
    const codes = [
      'dialog.detail_query',
      'dialog.edit',
      'message.update',
      'message.success',
      'message.api_info',
      'api_test_page',
    ];
    const dict = await this.tool.getDict(codes);
    // this.display=true;
    switch (operation) {
      case FormOperate.test:
        if (type == 'win') {
          const ref = this.dialogService.open(ApiTestComponent, {
            header: dict['api_test_page'],
            styleClass: 'cHeader',
            autoZIndex: false,

            data: {
              apikey: rowData.apiKey.ori
                ? rowData.apiKey.ori
                : rowData.apiKey.val,
              // rowData.apiKey.t == true
              //   ? rowData.apiKey.ori
              //   : rowData.apiKey.val,
              moduleName: rowData.moduleName.ori
                ? rowData.moduleName.ori
                : rowData.moduleName.val,
              // rowData.moduleName.t == true
              //   ? rowData.moduleName.ori
              //   : rowData.moduleName.val,
              apiSrc: rowData.apiSrc.v,
            },
            width: '90vw',
          });
        } else if (type == 'page') {
          this.pageNum = 4;
          this.initData = {
            apikey: rowData.apiKey.ori
              ? rowData.apiKey.ori
              : rowData.apiKey.val,
            // (rowData.apiKey.t == true
            //   ? rowData.apiKey.ori
            //   : rowData.apiKey.val) ?? '',
            moduleName: rowData.moduleName.ori
              ? rowData.moduleName.ori
              : rowData.moduleName.val,
            // (rowData.moduleName.t == true
            //   ? rowData.moduleName.ori
            //   : rowData.moduleName.val) ?? '',
            apiSrc: rowData.apiSrc.v,
          };
        } else {
          // this.router.navigate([
          //   '/ac03/ac0316',
          //   rowData.apiKey.t == true ? rowData.apiKey.ori : rowData.apiKey.val,
          //   rowData.moduleName.t == true
          //     ? rowData.moduleName.ori
          //     : rowData.moduleName.val,
          //   rowData.apiSrc.v,
          // ]);
        }
        break;
    }
  }

  jwtSettingOnChange(evn) {
    switch (this.pageNum) {
      case 1:
        if (evn.checked == true) {
          this.q_jweFlag!.enable();
          this.q_jweFlag!.setValue('0');
          this.q_jweFlagResp!.enable();
          this.q_jweFlagResp!.setValue('1');
        } else {
          this.q_jweFlag!.setValue('0');
          this.q_jweFlag!.disable();
          this.q_jweFlagResp!.setValue('0');
          this.q_jweFlagResp!.disable();
        }
        window.setTimeout(() => {
          this.jwtSettingChange();
        });
        break;
      case 3:
        if (evn.checked == true) {
          this.u_jweFlag!.enable();
          this.u_jweFlag!.setValue('0');
          this.u_jweFlagResp!.enable();
          this.u_jweFlagResp!.setValue('1');
        } else {
          this.u_jweFlag!.setValue('0');
          this.u_jweFlag!.disable();
          this.u_jweFlagResp!.setValue('0');
          this.u_jweFlagResp!.disable();
        }
        break;
    }
  }

  jweFlagOnChange(evn) {
    switch (this.pageNum) {
      case 1:
        if (evn.value == '0' && this.q_jweFlagResp!.value == '0') {
          this.q_jwtSetting!.setValue(false);
          this.q_jweFlag!.setValue('0');
          this.q_jweFlag!.disable();
          this.q_jweFlagResp!.setValue('0');
          this.q_jweFlagResp!.disable();
        }
        window.setTimeout(() => {
          this.jwtSettingChange();
        });
        break;
      case 3:
        if (evn.value == '0' && this.u_jweFlagResp!.value == '0') {
          this.u_jwtSetting!.setValue(false);
          this.u_jweFlag!.setValue('0');
          this.u_jweFlag!.disable();
          this.u_jweFlagResp!.setValue('0');
          this.u_jweFlagResp!.disable();
        }
        break;
    }
  }

  jweFlagRespOnChange(evn) {
    switch (this.pageNum) {
      case 1:
        if (evn.value == '0' && this.q_jweFlag!.value == '0') {
          this.q_jwtSetting!.setValue(false);
          this.q_jweFlag!.setValue('0');
          this.q_jweFlag!.disable();
          this.q_jweFlagResp!.setValue('0');
          this.q_jweFlagResp!.disable();
        }
        window.setTimeout(() => {
          this.jwtSettingChange();
        });
        break;
      case 3:
        if (evn.value == '0' && this.u_jweFlag!.value == '0') {
          this.u_jwtSetting!.setValue(false);
          this.u_jweFlag!.setValue('0');
          this.u_jweFlag!.disable();
          this.u_jweFlagResp!.setValue('0');
          this.u_jweFlagResp!.disable();
        }
        break;
    }
  }

  showJwtSetting() {
    this.q_jwtSetting!.setValue(false);
    this.q_jweFlag!.setValue('0');
    this.q_jweFlag!.disable();
    this.q_jweFlagResp!.setValue('0');
    this.q_jweFlagResp!.disable();
  }

  funFlagTransform() {
    if (this.apiDetail!.funFlag == 1) {
      this.tokenPayloadFlag = true;
    }
  }

  async copyData(data: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = data;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.messageService.add({
      severity: 'success',
      summary: `${dict['copy']} ${dict['data']}`,
      detail: `${dict['copy']} ${dict['message.success']}`,
    });
  }

  public changeSort(colum: any): void {
    const tempData = this.dataList.slice();
    const isAsc = this.direction === 'asc';

    // this.dataList = tempData.sort((a, b) => {
    //   switch (colum.field) {
    //     case 'apiKey': return this.compare(a.apiKey.val, b.apiKey.val, isAsc);
    //     case 'apiSrc': return this.compare(a.apiSrc.v, b.apiSrc.v, isAsc);
    //     case 'moduleName': return this.compare(a.moduleName.val, b.moduleName.val, isAsc);
    //     case 'apiName': return this.compare(a.apiName.val, b.apiName.val, isAsc);
    //   }
    // });
    this.dataList = tempData.sort((a, b) => {
      switch (colum) {
        case 'apiKey':
          const apiKeyA = a.apiKey.ori ? a.apiKey.ori : a.apiKey.val;
          const apiKeyB = b.apiKey.ori ? b.apiKey.ori : b.apiKey.val;
          return this.compare(apiKeyA, apiKeyB, isAsc);
        case 'apiSrc':
          return this.compare(a.apiSrc.v, b.apiSrc.v, isAsc);
        case 'moduleName':
          const moduleNameA = a.moduleName.ori
            ? a.moduleName.ori
            : a.moduleName.val;
          const moduleNameB = b.moduleName.ori
            ? b.moduleName.ori
            : b.moduleName.val;
          return this.compare(moduleNameA, moduleNameB, isAsc);
        case 'apiName':
          const apiNameA = a.apiName.ori ? a.apiName.ori : a.apiName.val;
          const apiNameB = b.apiName.ori ? b.apiName.ori : b.apiName.val;
          return this.compare(apiNameA, apiNameB, isAsc);
      }
      return 0;
    });
    if (isAsc == true) {
      this.direction = 'desc';
    } else {
      this.direction = 'asc';
    }
  }
  getApiGroupListEvt(keyword) {
    this.detailKeyword?.setValue(keyword);
    this.queryApiGroupList();
  }

  queryApiGroupList() {
    this.apiGroupList = [];
    this.apiGroupListRowcount = this.apiGroupList.length;
    let ReqBody = {
      keyword: this.detailKeyword!.value,
      moduleName:
        this.apiDetail!.moduleName.t == false
          ? this.apiDetail!.moduleName.v
          : this.apiDetail!.moduleName.o,
      apiKey:
        this.apiDetail!.apiKey.t == false
          ? this.apiDetail!.apiKey.v
          : this.apiDetail!.apiKey.o,
    } as AA0320Req;
    this.apiService.queryGroupApiList(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apiGroupList = res.RespBody.dataList;
        this.apiGroupListRowcount = this.apiGroupList.length;
      }
    });
  }

  moreApiGroupList() {
    let ReqBody = {
      keyword: this.detailKeyword!.value,
      gId: this.apiGroupList[this.apiGroupList!.length - 1].gId,
      moduleName:
        this.apiDetail!.moduleName.t == false
          ? this.apiDetail!.moduleName.v
          : this.apiDetail!.moduleName.o,
      apiKey:
        this.apiDetail!.apiKey.t == false
          ? this.apiDetail!.apiKey.v
          : this.apiDetail!.apiKey.o,
    } as AA0320Req;
    this.apiService.queryGroupApiList(ReqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apiGroupList = this.apiGroupList.concat(res.RespBody.dataList);
        this.apiGroupListRowcount = this.apiGroupList.length;
      }
    });
  }

  async checkChips(evt) {
    if (evt.value.length > 20) {
      this.labelList.value.pop();
      this.showLabelList_tip = true;
    } else {
      this.showLabelList_tip = false;
    }
  }

  async updateApi() {
    // console.log(this.u_srcUrl!.value)
    // return;
    switch (this.apiDetail!.apiSrc.v) {
      case 'R':
      case 'C':
        let updateRCapiReqBody = {
          apiKey: this.apiDetail!.apiKey.t
            ? this.apiDetail!.apiKey.o
            : this.apiDetail!.apiKey.v,
          moduleName: this.apiDetail!.moduleName.t
            ? this.apiDetail!.moduleName.o
            : this.apiDetail!.moduleName.v,
          apiName: this.u_apiName!.value,
          apiStatus: this.u_apiStatus!.value ? '1' : '2',
          jweFlag:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.u_jweFlag!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.u_jweFlag!.value
            ),
          jweFlagResp:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.u_jweFlagResp!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.u_jweFlagResp!.value
            ),
          srcUrl: this.u_srcUrl!.value,
          urlRID: this.u_urlRID!.value,
          noOAuth: this.u_noOAuth!.value,
          funFlag: {
            tokenPayload: this.u_tokenPayload!.value,
          },
          methodOfJson:
            this.apiDetail?.apiSrc.v === 'C'
              ? [this.u_methodOfJson!.value]
              : this.u_methodOfJson!.value,
          dataFormat:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.u_dataFormat!.value)
            ) +
            ',' +
            this.dataFormats.findIndex(
              (item) => item.value == this.u_dataFormat!.value
            ),
          // reghostId: this.u_regHostId!.value,
          apiDesc: this.u_apiDesc!.value,
          apiCacheFlag:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.apiCacheFlag!.value)
            ) +
            ',' +
            this.apiCacheFlags.findIndex(
              (item) => item.value == this.apiCacheFlag!.value
            ),
          mockStatusCode: this.u_mockStatusCode.value,
          mockBody: this.u_mockBody.value,
        } as AA0313Req;

        if (this.u_mockHeaders.value && this.u_mockHeaders.value != '') {
          updateRCapiReqBody.mockHeaders = this.u_mockHeaders.value;
        }

        if (this.apiDetail!.apiSrc.v == 'R') {
          updateRCapiReqBody.protocol = this.u_protocol!.value;
        }

        updateRCapiReqBody.redirectByIp = this.redirectByIp.value;

        if (updateRCapiReqBody.redirectByIp) {
          //檢核***
          this.redirectByIpDataList.value.forEach(async (item) => {
            // console.log(item)
            const code = ['sourceIpRequired', 'ipSrcUrlRequired'];
            const dict = await this.tool.getDict(code);
            if (item.ipForRedirect == '') {
              this.alert.ok(dict['sourceIpRequired'], '');
              return;
            }
            if (item.ipSrcUrl == '') {
              this.alert.ok(dict['ipSrcUrlRequired'], '');
              return;
            }
          });
          updateRCapiReqBody.redirectByIpDataList =
            this.redirectByIpDataList.value;
        }

        updateRCapiReqBody.headerMaskPolicy = this.headerMaskPolicy.value;
        if (updateRCapiReqBody.headerMaskPolicy != '0') {
          updateRCapiReqBody.headerMaskPolicyNum =
            this.headerMaskPolicyNum.value;
          updateRCapiReqBody.headerMaskPolicySymbol =
            this.headerMaskPolicySymbol.value;

          if (
            !this.headerMaskKey.value ||
            this.headerMaskKey.value.length == 0
          ) {
            const code = ['mask.header_key_required'];
            const dict = await this.tool.getDict(code);
            this.alert.ok(dict['mask.header_key_required'], '');
            return;
          }
          updateRCapiReqBody.headerMaskKey = this.headerMaskKey.value; //.join(',');
        }

        updateRCapiReqBody.bodyMaskPolicy = this.bodyMaskPolicy.value;
        if (updateRCapiReqBody.bodyMaskPolicy != '0') {
          updateRCapiReqBody.bodyMaskPolicyNum = this.bodyMaskPolicyNum.value;
          updateRCapiReqBody.bodyMaskPolicySymbol =
            this.bodyMaskPolicySymbol.value;

          if (
            !this.bodyMaskKeyword.value ||
            this.bodyMaskKeyword.value.trim() == ''
          ) {
            const code = ['mask.body_key_required'];
            const dict = await this.tool.getDict(code);
            this.alert.ok(dict['mask.body_key_required'], '');
            return;
          }
          updateRCapiReqBody.bodyMaskKeyword = this.bodyMaskKeyword.value;
        }

        updateRCapiReqBody.labelList = this.labelList.value;

        // console.log('fixedCacheTime', parseInt(this.fixedCacheTime.value))

        // if(this.apiCacheFlag!.value === '3' && parseInt(this.fixedCacheTime.value)==0){
        //   const code = ['fixed_cache_time_required']
        //   const dict = await this.tool.getDict(code);
        //   this.alert.ok(dict['fixed_cache_time_required'],'')
        //   return;
        // }
        updateRCapiReqBody.fixedCacheTime = parseInt(this.fixedCacheTime.value);

        updateRCapiReqBody.failDiscoveryPolicy = this.failDiscoveryPolicy.value;
        updateRCapiReqBody.failHandlePolicy = this.failHandlePolicy.value;
        updateRCapiReqBody.notifyNameList = this.notifyNameList.value;

        updateRCapiReqBody.isCorsAllowHeaders = this.isCorsAllowHeaders.value;
        updateRCapiReqBody.isCorsAllowMethods = this.isCorsAllowMethods.value;
        updateRCapiReqBody.isCorsAllowOrigin = this.isCorsAllowOrigin.value;

        updateRCapiReqBody.corsAllowHeaders = this.corsAllowHeaders.value ?? '';
        updateRCapiReqBody.corsAllowMethods = this.corsAllowMethods.value ?? '';
        updateRCapiReqBody.corsAllowOrigin = this.corsAllowOrigin.value ?? '';

        this.apiService
          .updateRegCompAPI(updateRCapiReqBody)
          .subscribe(async (res) => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
              const code = [
                'message.update',
                'message.api_info',
                'message.success',
              ];
              const dict = await this.tool.getDict(code);
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']} ${dict['message.api_info']}`,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });

              this.apiService.queryAllLabel_ignore1298().subscribe((res) => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                  this.lblList = res.RespBody.labelList.map((item) => {
                    return { label: item, value: item };
                  });

                  this.selLabelList = this.selLabelList.filter((selItem) =>
                    this.lblList.map((x) => x.label).includes(selItem)
                  );
                  this.clearFilter();

                  this.searchByLabel
                    ? this.queryAPIListByLabel(this.selLabelList)
                    : this.submitForm();
                  this.changePage('query');
                } else {
                  this.lblList = [];
                  this.selLabelList = [];
                  this.clearFilter();
                  this.submitForm();
                  this.changePage('query');
                }
              });
            }
          });
        break;
      case 'M':
      case 'N':
      default:
        let updateJavaApiReqBody = {
          apiKey: this.apiDetail!.apiKey.t
            ? this.apiDetail!.apiKey.o
            : this.apiDetail!.apiKey.v,
          moduleName: this.apiDetail!.moduleName.t
            ? this.apiDetail!.moduleName.o
            : this.apiDetail!.moduleName.v,
          apiName: this.u_apiName!.value,
          apiStatus: this.u_apiStatus!.value ? '1' : '2',
          jweFlag:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.u_jweFlag!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.u_jweFlag!.value
            ),
          jweFlagResp:
            this.tool.Base64Encoder(
              this.tool.BcryptEncoder(this.u_jweFlagResp!.value)
            ) +
            ',' +
            this.updateJwtSettingFlags.findIndex(
              (item) => item.value == this.u_jweFlagResp!.value
            ),
          apiDesc: this.u_apiDesc!.value,
        } as AA0304Req;
        this.apiService
          .updateAPIInfo(updateJavaApiReqBody)
          .subscribe(async (res) => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
              const code = [
                'message.update',
                'message.api_info',
                'message.success',
              ];
              const dict = await this.tool.getDict(code);
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']} ${dict['message.api_info']}`,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });

              this.apiService.queryAllLabel_ignore1298().subscribe((res) => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                  this.lblList = res.RespBody.labelList.map((item) => {
                    return { label: item, value: item };
                  });

                  this.selLabelList = this.selLabelList.filter((selItem) =>
                    this.lblList.map((x) => x.label).includes(selItem)
                  );
                  this.clearFilter();

                  this.searchByLabel
                    ? this.queryAPIListByLabel(this.selLabelList)
                    : this.submitForm();
                  this.changePage('query');
                } else {
                  this.lblList = [];
                  this.selLabelList = [];
                  this.clearFilter();
                  this.submitForm();
                  this.changePage('query');
                }
              });
            }
          });
        break;
    }
  }

  settingEditorStatus(controlName: object) {
    Object.keys(controlName).map((key) => {
      // if (key == 'regHostId') {
      //   if (controlName['regHostId']) {
      //     this.updateForm.get('reghostId')!.enable();
      //   } else {
      //     this.updateForm.get('reghostId')!.disable();
      //   }
      // } else {
      if (controlName[key]) {
        this.updateForm.get(key)!.enable();
      } else {
        this.updateForm.get(key)!.disable();
      }
      // }
    });
  }

  openComposer() {
    let _moduleName = this.apiDetail!.moduleName.t
      ? this.apiDetail!.moduleName.o
      : this.apiDetail!.moduleName.v;
    let _apiKey = this.apiDetail!.apiKey.t
      ? this.apiDetail!.apiKey.o
      : this.apiDetail!.apiKey.v;
    // let ReqBody = {
    //     authType: 'Composer',
    //     resource: _moduleName,
    //     subclass: _apiKey
    // } as AA0511Req;
    // this.utilService.getAuthCode_v3(ReqBody).subscribe(res => {
    //     if (this.tool.checkDpSuccess(res.ResHeader)) {
    //         let url = `${location.protocol}//${location.hostname}:${res.RespBody.targetPort}${res.RespBody.targetPath}?ac=${res.RespBody.authCode}&moduleName=${_moduleName}&apiKey=${_apiKey}`;
    //         window.open(url);
    //     }
    // });

    //20220523 替換api 0511 => 0142
    let _reqBody = {
      resource: _moduleName,
      subclass: _apiKey,
    } as DPB0142Req;

    this.serverService.getACEntryTicket(_reqBody).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const composerParam = {
          port: res.RespBody.targetPort,
          path: res.RespBody.targetPath,
          apiUid: res.RespBody.apiUid,
          authCode: res.RespBody.authCode,
          moduleName: _moduleName!,
          apiKey: _apiKey!,
        };
        const url = this.tool.composerUrl(composerParam);
        // let url = `${location.protocol}//${location.hostname}:${location.port}/website/composer/${res.RespBody.apiUid}?ac=${res.RespBody.authCode}&moduleName=${_moduleName}&apiKey=${_apiKey}`;
        window.open(url);
      }
    });
  }

  async changePage(action: string, rowData?: AA0301Item) {
    this.showLabelList_tip = false;
    const code = ['button.detail', 'button.update', 'upload_reg_comp_api_file'];
    const dict = await this.tool.getDict(code);
    this.resetFormValidator(this.detailForm);
    if (this.jwtSettingSub) {
      this.jwtSettingSub.unsubscribe();
      this.jwtSettingSub = undefined;
    }
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'detail':
        if (!rowData) return;
        document.querySelector('section')?.scrollTo(0, 0);
        let detailReqBody = {
          moduleName: rowData.moduleName.ori
            ? rowData.moduleName.ori
            : rowData.moduleName.val,
          // rowData.moduleName.t == false
          //   ? rowData.moduleName.val
          //   : rowData.moduleName.ori,
          apiKey: rowData.apiKey.ori ? rowData.apiKey.ori : rowData.apiKey.val,
          // rowData.apiKey.t == false ? rowData.apiKey.val : rowData.apiKey.ori,
        } as AA0302Req;
        this.apiService.queryAPIDetail_v3(detailReqBody).subscribe((res) => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.apiDetail = res.RespBody;
            // console.log(this.apiDetail)
            this.currentTitle = `${this.title} > ${dict['button.detail']}`;
            this.pageNum = 2;
            this.funFlagTransform();
            this.apiGroupList = [];
            this.apiGroupListRowcount = this.apiGroupList.length;
            this.procSrcUrl(this.apiDetail.srcUrl);
            // console.log(this.apiDetail.redirectByIpDataList)
            if (this.apiDetail.isRedirectByIp) {
              this.generateIPSrcUrl(this.apiDetail.redirectByIpDataList);
            }

            if (!res.RespBody.headerMaskPolicy)
              this.apiDetail.headerMaskPolicy = '0';
            if (!res.RespBody.bodyMaskPolicy)
              this.apiDetail.bodyMaskPolicy = '0';

            this.labelList.setValue(this.apiDetail.labelList);
            this.fixedCacheTime.setValue(this.apiDetail.fixedCacheTime);

            let ReqBody = {
              keyword: this.detailKeyword!.value,
              moduleName: this.apiDetail.moduleName.o
                ? this.apiDetail.moduleName.o
                : this.apiDetail.moduleName.v,
              // this.apiDetail.moduleName.t == false
              //   ? this.apiDetail.moduleName.v
              //   : this.apiDetail.moduleName.o,
              apiKey: this.apiDetail.apiKey.o
                ? this.apiDetail.apiKey.o
                : this.apiDetail.apiKey.v,
              // this.apiDetail.apiKey.t == false
              //   ? this.apiDetail.apiKey.v
              //   : this.apiDetail.apiKey.o,
            } as AA0320Req;
            this.apiService.queryGroupApiList_ajax(ReqBody).then((res) => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.apiGroupList = res.RespBody.dataList;
                this.apiGroupListRowcount = this.apiGroupList.length;

                document.querySelector('section')?.scrollTo(0, 0);
              } else {
                if (res.ResHeader.rtnCode != '1298') {
                  this.alert.ok(
                    `Return code : ${res.ResHeader.rtnCode}`,
                    res.ResHeader.rtnMsg
                  ); // 判斷後兩碼如不為00 , show rtnMsg
                }
              }
            });
          }
        });
        break;
      case 'update':
        if (!rowData) return;
        this.resetFormValidator(this.updateForm);
        this.updateForm.enable();
        this.isWebhook = false;
        this.isAiGateway = false;
        // 註冊主機
        // this.regService
        //   .queryRegHostList_1_ignore1298({ paging: 'false' } as AA0806Req)
        //   .subscribe((res) => {
        //     if (this.tool.checkDpSuccess(res.ResHeader)) {
        //       let _reghostIds: { label: string; value: string }[] = [];
        //       res.RespBody.hostInfoList.map((item) => {
        //         _reghostIds.push({
        //           label: item.regHost,
        //           value: item.regHostID,
        //         });
        //       });
        //       this.reghostIds = _reghostIds;
        //     }
        //   });
        let updateDetailReqBody = {
          moduleName: rowData.moduleName.ori
            ? rowData.moduleName.ori
            : rowData.moduleName.val,
          // rowData.moduleName.t == false
          //   ? rowData.moduleName.val
          //   : rowData.moduleName.ori,
          apiKey: rowData.apiKey.ori ? rowData.apiKey.ori : rowData.apiKey.val,
          // rowData.apiKey.t == false ? rowData.apiKey.val : rowData.apiKey.ori,
        } as AA0302Req;
        this.apiService
          .queryAPIDetail_v3(updateDetailReqBody)
          .subscribe((res) => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
              this.apiDetail = res.RespBody;
              this.currentTitle = `${this.title} > ${dict['button.update']}`;
              this.pageNum = 3;

              this.apiCacheFlag.setValue(this.apiDetail.apiCacheFlag.v);
              this.fixedCacheTime.setValue(this.apiDetail.fixedCacheTime);

              this.u_apiKey!.setValue(
                this.apiDetail.apiKey.t
                  ? this.apiDetail.apiKey.o
                  : this.apiDetail.apiKey.v
              );
              this.u_moduleName!.setValue(
                this.apiDetail.moduleName.t
                  ? this.apiDetail.moduleName.o
                  : this.apiDetail.moduleName.v
              );
              this.u_apiName!.setValue(
                this.apiDetail.apiName.t
                  ? this.apiDetail.apiName.o
                  : this.apiDetail.apiName.v
              );
              this.u_apiStatus!.setValue(
                this.apiDetail.apiStatus.v == '1' ? true : false
              );
              this.u_jweFlag!.setValue(this.apiDetail.jweFlag.v);
              this.u_jweFlagResp!.setValue(this.apiDetail.jweFlagResp.v);
              if (
                (this.u_jweFlag!.value == '0' ||
                  this.u_jweFlag!.value == null) &&
                (this.u_jweFlagResp!.value == '0' ||
                  this.u_jweFlagResp!.value == null)
              ) {
                this.u_jwtSetting!.setValue(false);
              } else {
                this.u_jwtSetting!.setValue(true);
              }
              if (
                (this.u_jweFlag!.value == '0' ||
                  this.u_jweFlag!.value == null) &&
                (this.u_jweFlagResp!.value == '0' ||
                  this.u_jweFlagResp!.value == null)
              ) {
                this.u_jweFlag!.disable();
              } else {
                this.u_jweFlag!.enable();
              }
              if (
                (this.u_jweFlag!.value == '0' ||
                  this.u_jweFlag!.value == null) &&
                (this.u_jweFlagResp!.value == '0' ||
                  this.u_jweFlagResp!.value == null)
              ) {
                this.u_jweFlagResp!.disable();
              } else {
                this.u_jweFlagResp!.enable();
              }
              this.jwtSettingSub = this.u_jwtSetting?.valueChanges.subscribe(
                (states) => {
                  // console.log(states);
                  if (states) {
                    this.u_jweFlag!.enable();
                    this.u_jweFlag!.setValue('0');
                    this.u_jweFlagResp!.enable();
                    this.u_jweFlagResp!.setValue('1');
                  } else {
                    this.u_jweFlag!.setValue('0');
                    this.u_jweFlag!.disable();
                    this.u_jweFlagResp!.setValue('0');
                    this.u_jweFlagResp!.disable();
                  }
                }
              );

              if (
                this.apiDetail.apiSrc.v == 'R' ||
                this.apiDetail.apiSrc.v == 'C'
              ) {
                this.u_urlRID!.setValue(
                  this.apiDetail.urlRID == '1' ? true : false
                );
                this.u_noOAuth!.setValue(
                  this.apiDetail.noOAuth == '1' ? true : false
                );
                this.u_tokenPayload!.setValue(
                  this.apiDetail.funFlag == 1 ? true : false
                );
                if (this.apiDetail.protocol) {
                  let _protocol = this.apiDetail.protocol.split('://')[0];
                  this.u_protocol!.setValue(_protocol);
                }
                // if (this.apiDetail?.srcUrl?.t) {

                //   if (this.apiDetail!.srcUrl?.o?.includes('://')) {
                //     this.u_srcUrl!.setValue(
                //       this.apiDetail.srcUrl.o.split('://')[1]
                //     );

                //   } else {
                //     this.u_srcUrl!.setValue(this.apiDetail.srcUrl.o);
                //   }

                // } else {
                //   if (this.apiDetail?.srcUrl?.v.includes('://')) {
                //     this.u_srcUrl!.setValue(
                //       this.apiDetail.srcUrl.v.split('://')[1]
                //     );
                //   } else {
                //     this.u_srcUrl!.setValue(this.apiDetail?.srcUrl?.v);
                //   }
                // }
                if (this.apiDetail?.srcUrl?.o) {
                  this.u_srcUrl?.setValue(this.apiDetail?.srcUrl?.o);
                } else {
                  this.u_srcUrl?.setValue(this.apiDetail?.srcUrl?.v);
                }
                /** 用來切換輸入元件，若srcUrl為b64開頭則切換為批次輸入元件*/
                // if(this.u_srcUrl?.value.substring(0,3)=='b64'){
                //   this.srcUrlB64Proc = true;
                // }

                let _methodOfJson =
                  this.apiDetail.apiSrc.v !== 'C'
                    ? this.apiDetail?.methodOfJson?.split(',')
                    : this.apiDetail?.methodOfJson;
                this.u_methodOfJson!.setValue(_methodOfJson);
                this.u_dataFormat!.setValue(this.apiDetail.dataFormat.v);
                // this.u_regHostId!.setValue(this.apiDetail.regHostId);
              }
              this.u_apiDesc!.setValue(this.apiDetail.apiDesc);

              this.u_mockStatusCode.setValue(this.apiDetail.mockStatusCode);
              this.u_mockBody.setValue(this.apiDetail.mockBody);
              this.u_mockHeaders.setValue(this.apiDetail.mockHeaders);

              this.settingEditorStatus(this.apiDetail.controls);

              switch (this.apiDetail.apiSrc.v) {
                case 'R':
                case 'C':
                  this.apiService.updateRegCompAPI_before().subscribe((res) => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                      this.addFormValidator(
                        this.updateForm,
                        res.RespBody.constraints
                      );
                    }
                  });
                  break;
                case 'M':
                case 'N':
                default:
                  this.apiService.updateAPIInfo_before().subscribe((res) => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                      this.addFormValidator(
                        this.updateForm,
                        res.RespBody.constraints
                      );
                    }
                  });
                  break;
              }

              // source ip

              this.redirectByIp.setValue(res.RespBody.isRedirectByIp);

              this.redirectByIpDataList.setValue(
                res.RespBody.redirectByIpDataList
                  ? res.RespBody.redirectByIpDataList.map((row) => {
                      return {
                        ipForRedirect: row.ipForRedirect,
                        ipSrcUrl: row.ipSrcUrl.t
                          ? row.ipSrcUrl.o
                          : row.ipSrcUrl.v,
                      };
                    })
                  : []
              );
              this.headerMaskPolicy.setValue(
                res.RespBody.headerMaskPolicy
                  ? res.RespBody.headerMaskPolicy
                  : '0'
              );
              this.headerMaskPolicyNum.setValue(
                res.RespBody.headerMaskPolicyNum
              );
              this.headerMaskPolicySymbol.setValue(
                res.RespBody.headerMaskPolicySymbol
              );
              this.headerMaskKey.setValue(res.RespBody.headerMaskKey);
              this.bodyMaskPolicy.setValue(
                res.RespBody.bodyMaskPolicy ? res.RespBody.bodyMaskPolicy : '0'
              );
              this.bodyMaskPolicyNum.setValue(res.RespBody.bodyMaskPolicyNum);
              this.bodyMaskPolicySymbol.setValue(
                res.RespBody.bodyMaskPolicySymbol
              );
              this.bodyMaskKeyword.setValue(res.RespBody.bodyMaskKeyword);
              // console.log(this.redirectByIpDataList.value)
              this.labelList.setValue(this.apiDetail.labelList);

              this.failDiscoveryPolicy.setValue(
                this.apiDetail.failDiscoveryPolicy
              );
              this.failHandlePolicy.setValue(this.apiDetail.failHandlePolicy);
              if(this.apiDetail.notifyNameList) this.notifyNameList.setValue([...this.apiDetail.notifyNameList]);

              const _srcURl = this.apiDetail?.srcUrl?.o
                ? this.apiDetail.srcUrl.o
                : this.apiDetail?.srcUrl!.v;
              //check ai-gateway
              this.isAiGateway = this.tool.checkSrcUrlisAiGateway(_srcURl);

              //check webhook
              this.isWebhook = this.tool.checkSrcUrlisWebhook(_srcURl);

              if (this.isWebhook) {
                this.u_srcUrl?.disable();
              } else {
                this.u_srcUrl?.enable();
              }

              this.isCorsAllowOrigin.setValue(res.RespBody.isCorsAllowOrigin);
              if (this.isCorsAllowOrigin.value == 'Y') {
                this.corsAllowOrigin!.setValidators(
                  ValidatorFns.requiredValidator()
                );
              }
              this.isCorsAllowOrigin.valueChanges.subscribe((res) => {
                if (res == 'Y') {
                  this.corsAllowOrigin!.setValidators(
                    ValidatorFns.requiredValidator()
                  );
                } else {
                  this.corsAllowOrigin.clearValidators();
                  this.corsAllowOrigin.updateValueAndValidity();
                }
              });

              this.isCorsAllowMethods.setValue(res.RespBody.isCorsAllowMethods);
              if (this.isCorsAllowMethods.value == 'Y') {
                this.corsAllowMethods!.setValidators(
                  ValidatorFns.requiredValidator()
                );
              }
              this.isCorsAllowMethods.valueChanges.subscribe((res) => {
                if (res == 'Y') {
                  this.corsAllowMethods!.setValidators(
                    ValidatorFns.requiredValidator()
                  );
                } else {
                  this.corsAllowMethods.clearValidators();
                  this.corsAllowMethods.updateValueAndValidity();
                }
              });

              this.isCorsAllowHeaders.setValue(res.RespBody.isCorsAllowHeaders);
              if (this.isCorsAllowHeaders.value == 'Y') {
                this.corsAllowHeaders!.setValidators(
                  ValidatorFns.requiredValidator()
                );
              }
              this.isCorsAllowHeaders.valueChanges.subscribe((res) => {
                if (res == 'Y') {
                  this.corsAllowHeaders!.setValidators(
                    ValidatorFns.requiredValidator()
                  );
                } else {
                  this.corsAllowHeaders.clearValidators();
                  this.corsAllowHeaders.updateValueAndValidity();
                }
              });

              this.corsAllowHeaders.setValue(res.RespBody.corsAllowHeaders);
              this.corsAllowMethods.setValue(res.RespBody.corsAllowMethods);
              this.corsAllowOrigin.setValue(res.RespBody.corsAllowOrigin);

            }
          });
        break;
      case 'swagger':
        let tarUrl: string = '';
        if (
          location.hostname == 'localhost' ||
          location.hostname == '127.0.0.1'
        ) {
          tarUrl = environment.apiUrl;
        } else {
          tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
        }

        let _moduleName: string = rowData!.moduleName.ori
          ? rowData!.moduleName.ori
          : rowData!.moduleName.val;
        let _apiKey: string = rowData!.apiKey.ori
          ? rowData!.apiKey.ori
          : rowData!.apiKey.val;

        tarUrl =
          tarUrl +
          `/composer/swagger3.0/${_moduleName}/${_apiKey}/swagger.json`;
        // window.open(tarUrl);
        const ref = this.dialogService.open(SwaggerComponent, {
          header: 'Swagger',
          styleClass: 'cHeader',
          data: {
            tarUrl: tarUrl,
          },
          width: '90vw',
          height: '100vh',
        });
        break;
      case 'import':
        this.resetFormValidator(this.form);
        this.apiFile = undefined;
        $('#file').val('');
        this.import_apiList = [];
        this.currentTitle = `${this.title} > ${dict['upload_reg_comp_api_file']}`;
        this.pageNum = 5;

        break;
    }
  }

  // checkSrcUrlisAiGateway(_srcUrl: string): boolean {
  //   let isAiGateway: boolean = false;
  //   if (_srcUrl.includes('b64.')) {
  //     if (_srcUrl.substring(0, 3) == 'b64') {
  //       let srcUrlArr = _srcUrl.split('.');
  //       srcUrlArr.shift();

  //       let srcUrlArrEdit: { percent: string; url: string }[] = [];
  //       for (let i = 0; i < srcUrlArr.length; i++) {
  //         if (i % 2 == 0) {
  //           srcUrlArrEdit.push({
  //             percent: srcUrlArr[i],
  //             url: base64.Base64.decode(srcUrlArr[i + 1]),
  //           });
  //         }
  //       }

  //       isAiGateway = srcUrlArrEdit.some((item) => {
  //         const dgrProtocol_match = this.toolServiceW.dgrProtocol_match(
  //           item.url
  //         );
  //         return dgrProtocol_match.some((item) => item.includes('ai-gateway'));
  //       });
  //     }
  //   } else {
  //     const dgrProtocol_match = this.toolServiceW.dgrProtocol_match(_srcUrl);
  //     isAiGateway = dgrProtocol_match.some((item) =>
  //       item.includes('ai-gateway')
  //     );
  //   }
  //   return isAiGateway;
  // }

  originStringTable(item: any) {
    return item ? (!item.ori ? item.val : item.t ? item.val : item.ori) : '';
  }

  originString(item: any) {
    let str: String = item.o ? (item.t == true ? item.v : item.o) : item.v;
    return !str || str == '[]' || str == '' ? ' - ' : str;
  }

  switchOri(item: any) {
    item.t = !item.t;
  }

  headerReturn() {
    if (this.reloadFlag) {
      this.reloadFlag = false;
      this.queryAllLabel();
      this.searchByLabel
        ? this.queryAPIListByLabel(this.selLabelList)
        : this.submitForm();
    }
    this.changePage('query');
  }

  procSrcUrl(srcUrlObj) {
    let srcUrl = srcUrlObj.o ? srcUrlObj.o : srcUrlObj.v;
    this.srcUrlPool = [];

    if (srcUrl.includes('b64.')) {
      let _srcUrl = srcUrl.split('b64.')[1];
      let srcUrlArr = srcUrl.split('.');
      srcUrlArr.shift();

      for (let i = 0; i < srcUrlArr.length; i++) {
        if (i % 2 == 0) {
          this.srcUrlPool.push({
            percent: srcUrlArr[i],
            //  url: this.tool.Base64Decoder(srcUrlArr[i + 1])
            url: base64.Base64.decode(srcUrlArr[i + 1]),
          });
        }
      }
    } else {
      this.srcUrlPool.push({ percent: '100', url: srcUrl });
    }
  }

  formatSrcUrl(srcUrlObj) {
    let _srcUrl = srcUrlObj.o ? srcUrlObj.o : srcUrlObj.v;
    let _srcUrlPool: { percent: string; url: string }[] = [];

    if (_srcUrl.includes('b64.')) {
      let srcUrlArr = _srcUrl.split('.');
      srcUrlArr.shift();
      for (let i = 0; i < srcUrlArr.length; i++) {
        if (i % 2 == 0) {
          _srcUrlPool.push({
            percent: srcUrlArr[i],
            url: base64.Base64.decode(srcUrlArr[i + 1]),
          });
        }
      }
    } else {
      _srcUrlPool.push({ percent: '100', url: _srcUrl });
    }
    return _srcUrlPool;
  }

  generateIPSrcUrl(ipSrcUrl: Array<AA0302RedirectByIpData>) {
    // console.log(ipSrcUrl)
    this.ipSrcUrl = ipSrcUrl.map((row) => {
      return {
        ipForRediret: row.ipForRedirect,
        srcUrlPool: this.formatSrcUrl(row.ipSrcUrl),
      };
    });
    // console.log(this.ipSrcUrl)
  }

  checkheaderMaskPolicySymbol() {
    if (
      this.headerMaskPolicySymbol.value == undefined ||
      this.headerMaskPolicySymbol.value.trim() == ''
    ) {
      this.headerMaskPolicySymbol.setValue('*');
    }
  }

  checkbodyMaskPolicySymbol() {
    if (
      this.bodyMaskPolicySymbol.value == undefined ||
      this.bodyMaskPolicySymbol.value.trim() == ''
    ) {
      this.bodyMaskPolicySymbol.setValue('*');
    }
  }

  async queryLabelList() {
    this.queryAPIListByLabel(this.selLabelList);
    // const code = ['label_list'];
    // const dict = await this.tool.getDict(code);
    // const ref = this.dialogService.open(LabelListComponent, {
    //   header: dict['label_list'],
    //   width: '700px',
    // });

    // ref.onClose.subscribe((res) => {
    //   if (res && res.length > 0) {
    //     this.selLabelList = res;
    //     this.queryAPIListByLabel(res);
    //   } else {
    //     this.selLabelList = [];
    //   }
    // });
  }

  queryAPIListByLabel(labelList: Array<string>) {
    this.selected = [];
    this.dataList = [];
    this.rowcount = this.dataList.length;
    let req = {
      labelList: labelList,
      paging: 'Y',
    } as AA0428Req;
    this.apiService.queryAPIListByLabel(req).subscribe((res) => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.dataList;
        this.rowcount = this.dataList.length;
        this.lastRowdata = this.dataList[this.dataList.length - 1];
      }
      this.searchByLabel = true;
    });
  }

  tabChange(evt) {
    if (evt.index == 0) {
      this.submitForm();
    } else {
      this.selected = [];
      this.dataList = [];
      this.selLabelList = [];
      this.queryAllLabel();
    }
  }

  clearFilter() {
    if (this.lableFilter) {
      this.lableFilter.filterValue = '';
      this.lableFilter.updateFilledState();
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  changeFile(event) {
    if (event.target.files.length != 0) {
      this.apiFile = event.target.files[0];
      this.fileName.setValue(this.apiFile!.name);
      let _fileSize = this.apiFile!.size / 1024;
      this.fileSize.setValue(Math.round(_fileSize * 100) / 100);
    } else {
      this.file.reset();
      this.fileName.setValue('');
      this.fileSize.setValue('');
    }
  }

  uploadFile() {
    let fileReader = new FileReader();
    fileReader.onloadend = () => {
      this.fileService.uploadFile2(this.apiFile!).subscribe((res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.import_apiList = [];
          this.ngxService.start();
          let ReqBody = {
            tempFileName: res.RespBody.tempFileName,
          } as AA0318Req;
          this.apiService.uploadRegCompAPIs(ReqBody).subscribe(async (resp) => {
            this.ngxService.stop();
            if (this.tool.checkDpSuccess(resp.ResHeader)) {
              const code = [
                'uploading',
                'waiting',
                'cfm_size',
                'upload_result',
                'message.success',
              ];
              const dict = await this.tool.getDict(code);
              this.messageService.add({
                severity: 'success',
                summary: dict['upload_result'],
                detail: `${dict['message.success']}!`,
              });
              this.fileBatchNo = resp.RespBody.batchNo;

              resp.RespBody.apiList.map((rowData) => {
                if (rowData.srcURLByIpRedirectMap) {
                  let tmp = <any>[];
                  Object.keys(rowData.srcURLByIpRedirectMap)
                    .sort()
                    .map((key) => {
                      // console.log('key',key);
                      tmp.push({
                        ip: key,
                        srcURL: rowData.srcURLByIpRedirectMap![key],
                      });
                    });
                  rowData.srcURLByIpRedirectMap = tmp;
                }
                return {
                  ...rowData,
                };
              });
              this.import_apiList = resp.RespBody.apiList;
            }
          });
        }
      });
    };
    fileReader.readAsText(this.apiFile!);
  }

  decodeSrcUrl(srcUrl) {
    if (srcUrl.includes('b64.')) {
      let srcUrlArr = srcUrl.split('.');
      srcUrlArr.shift();

      let srcUrlArrEdit: string[] = [];
      for (let i = 0; i < srcUrlArr.length; i++) {
        if (i % 2 == 0) {
          srcUrlArrEdit.push(
            srcUrlArr[i] + '%, ' + base64.Base64.decode(srcUrlArr[i + 1])
          );
        }
      }

      return srcUrlArrEdit.join('<br>');
    } else {
      return srcUrl;
    }
  }

  async import() {
    const code = [
      'button.import',
      'data',
      'message.success',
      'unchecked_api',
      'cfm_import',
      'message.fail',
    ];
    const dict = await this.tool.getDict(code);

    if (this.import_selected.length < this.import_apiList.length) {
      this.confirmationService.confirm({
        header: dict['unchecked_api'],
        message: dict['cfm_import'],
        key: 'cd',
        accept: () => {
          this.confirmImport();
        },
      });
    } else {
      this.ngxService.start();
      let _apiList = this.import_selected.map((item) => {
        return {
          moduleName: item.moduleName.t
            ? item.moduleName.ori
            : item.moduleName.val,
          apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val,
        } as AA0319ReqItem;
      });
      let ReqBody = {
        batchNo: this.fileBatchNo,
        apiList: _apiList,
      } as AA0319Req;
      this.apiService.importRegCompAPIs(ReqBody).subscribe((res) => {
        this.ngxService.stop();
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          for (let i = 0; i < res.RespBody.apiList.length; i++) {
            this.import_selected.forEach((item) => {
              if (
                (item.apiKey.t ? item.apiKey.ori : item.apiKey.val) ==
                  res.RespBody.apiList[i].apiKey &&
                (item.moduleName.t
                  ? item.moduleName.ori
                  : item.moduleName.val) == res.RespBody.apiList[i].moduleName
              ) {
                item['result'] = res.RespBody.apiList[i].result;

                if (res.RespBody.apiList[i].result.v == 'S') {
                  item['memo'] = {
                    t: false,
                    val: '',
                  };
                } else {
                  if (res.RespBody.apiList[i].desc) {
                    item['memo'] = res.RespBody.apiList[i].desc;
                  }
                }
              }
            });
          }
          if (res.RespBody.apiList.every((item) => item.result.v == 'S')) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['button.import']} ${dict['message.success']}`,
            });
          } else {
            this.messageService.add({
              severity: 'error',
              summary: `${dict['button.import']} ${dict['message.fail']}`,
            });
          }
          this.import_selected = [];
          this.reloadFlag = true;
        }
      });
    }
  }

  async confirmImport() {
    this.messageService.clear();
    const code = ['button.import', 'data', 'message.success', 'message.fail'];
    const dict = await this.tool.getDict(code);
    this.ngxService.start();
    let _apiList = this.import_selected.map((item) => {
      return {
        moduleName: item.moduleName.t
          ? item.moduleName.ori
          : item.moduleName.val,
        apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val,
      } as AA0319ReqItem;
    });
    let ReqBody = {
      batchNo: this.fileBatchNo,
      apiList: _apiList,
    } as AA0319Req;
    this.apiService.importRegCompAPIs(ReqBody).subscribe((res) => {
      this.ngxService.stop();
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        for (let i = 0; i < res.RespBody.apiList.length; i++) {
          this.selected.forEach((item) => {
            if (
              (item.apiKey.t ? item.apiKey.ori : item.apiKey.val) ==
                res.RespBody.apiList[i].apiKey &&
              (item.moduleName.t ? item.moduleName.ori : item.moduleName.val) ==
                res.RespBody.apiList[i].moduleName
            ) {
              item['result'] = res.RespBody.apiList[i].result;
              if (res.RespBody.apiList[i].result.v == 'S') {
                item['memo'] = {
                  t: false,
                  val: '',
                };
              } else {
                if (res.RespBody.apiList[i].desc) {
                  item['memo'] = res.RespBody.apiList[i].desc;
                }
              }
            }
          });
        }
        if (res.RespBody.apiList.every((item) => item.result.v == 'S')) {
          this.messageService.add({
            severity: 'success',
            summary: `${dict['button.import']} ${dict['message.success']}`,
          });
        } else {
          this.messageService.add({
            severity: 'error',
            summary: `${dict['button.import']} ${dict['message.fail']}`,
          });
        }
        this.import_selected = [];
        this.reloadFlag = true;
      }
    });
  }

  selectAll(evn) {
    this.import_selected = this.import_selected.filter(
      (item) => item.checkAct.v != 'N'
    );
  }

  viewMode: number = 0;
  ModeItems: MenuItem[] = [
    // {
    //   label: '管理檢視',
    //   command: () => this.setViewMode(0),
    //   styleClass:'active',
    // },
    // {
    //   label: '稽核檢視',
    //   command: () => this.setViewMode(1),
    // },
  ];
  loadMenu() {
    this.translate
      .get(['view_mode.0', 'view_mode.1', 'view_mode.2'])
      .subscribe((translations) => {
        this.ModeItems = [
          {
            label: `${translations['view_mode.0']}`,
            id: '0',
            command: () => this.setViewMode(0),
            styleClass: 'active',
          },
          {
            label: `${translations['view_mode.2']}`,
            id: '2',
            command: () => this.setViewMode(2),
          },
          {
            label: translations['view_mode.1'],
            id: '1',
            command: () => this.setViewMode(1),
          },
        ];
      });
  }
  setViewMode(mode: number) {
    this.viewMode = mode;
    // 重新賦值 ModeItems 來觸發變化檢測
    this.updateMenuItems(mode);
  }

  updateMenuItems(mode: number) {
    this.ModeItems.forEach(
      (x, i) => (x.styleClass = String(mode) === x.id ? 'active' : '')
    );
  }

  async updateJwtSetting() {
    const code = ['jwt_setting'];
    const dict = await this.tool.getDict(code);
    const ref = this.dialogService.open(JwtSettingComponent, {
      // styleClass: 'cHeader cContent cIcon',
      header: dict['jwt_setting'],
      width: '400px',
      // height: '30vh',
      data: {
        updateJwtSettingFlags: this.updateJwtSettingFlags,
      },
    });
    ref.onClose.subscribe((res) => {
      if (res) {
        this.q_jweFlag!.setValue(res.jweFlag);
        this.q_jweFlagResp!.setValue(res.jweFlagResp);
        this.jwtSettingChange();
      }
    });
  }

  queryNotifyNameList() {
    const ref = this.dialogService.open(NotifyListComponent, {
      header: 'Notify Name',
      width: '700px',
      data: {
        notifyNameList: this.notifyNameList.value
      }
    });

    ref.onClose.subscribe((res) => {
     if(res)  this.notifyNameList.setValue(res);
    });
  }

  orgReset() {
    this.orgService
      .queryOrgList({ orgID: undefined })
      .subscribe(async (res) => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          const _orgList = res.RespBody.orgList;

          const codes = ['org_chart'];
          const dict = await this.tool.getDict(codes);

          const refDialog = this.dialogService.open(OrganizationComponent, {
            header: dict['org_chart'],
            modal: true,
            data: {
              orgList: _orgList,
              showFooterBtn: true,
            },
            width: '90vw',
            height: '100vh',
          });

          refDialog.onClose.subscribe((res) => {
            if (res) {
              const reqArr = this.selected.map((rowData) => {
                return {
                  apiKey: rowData.apiKey?.ori
                    ? rowData.apiKey.ori
                    : rowData.apiKey.val,
                  moduleName: rowData.moduleName?.ori
                    ? rowData.moduleName.ori
                    : rowData.moduleName.val,
                  orgID: res.data.orgID,
                } as AA0434DTO;
              });

              this.orgService.batchApiOrg(reqArr).subscribe(async (res) => {
                if (this.tool.checkDpSuccess(res.ResHeader)) {
                  const code = ['message.success', 'org_reset'];
                  const dict = await this.tool.getDict(code);
                  this.messageService.add({
                    severity: 'success',
                    summary: `${dict['org_reset']}`,
                    detail: `${dict['org_reset']} ${dict['message.success']}`,
                  });

                  // 取回資料
                  this.searchByLabel
                    ? this.queryAPIListByLabel(this.selLabelList)
                    : this.submitForm();
                }
              });
            }
          });
        }
      });
  }
  public get q_jwtSetting() {
    return this.queryForm.get('jwtSetting');
  }
  public get q_jweFlag() {
    return this.queryForm.get('jweFlag');
  }
  public get q_jweFlagResp() {
    return this.queryForm.get('jweFlagResp');
  }
  public get q_queryJweFlag() {
    return this.queryForm.get('queryJweFlag');
  }
  public get q_queryJweFlagResp() {
    return this.queryForm.get('queryJweFlagResp');
  }
  public get apiSrc() {
    return this.queryForm.get('apiSrc');
  }
  public get queryKeyword() {
    return this.queryForm.get('keyword');
  }
  public get apiStatus() {
    return this.queryForm.get('apiStatus');
  }
  public get publicFlag() {
    return this.queryForm.get('publicFlag');
  }
  public get detailKeyword() {
    return this.detailForm.get('keyword');
  }
  public get u_jwtSetting() {
    return this.updateForm.get('jwtSetting');
  }
  public get u_jweFlag() {
    return this.updateForm.get('jweFlag');
  }
  public get u_jweFlagResp() {
    return this.updateForm.get('jweFlagResp');
  }
  public get u_apiKey() {
    return this.updateForm.get('apiKey');
  }
  public get u_moduleName() {
    return this.updateForm.get('moduleName');
  }
  public get u_apiName() {
    return this.updateForm.get('apiName');
  }
  public get u_apiStatus() {
    return this.updateForm.get('apiStatus');
  }
  public get u_protocol() {
    return this.updateForm.get('protocol');
  }
  public get u_srcUrl() {
    return this.updateForm.get('srcUrl');
  }
  public get u_urlRID() {
    return this.updateForm.get('urlRID');
  }
  public get u_noOAuth() {
    return this.updateForm.get('noOAuth');
  }
  public get u_tokenPayload() {
    return this.updateForm.get('tokenPayload');
  }
  public get u_methodOfJson() {
    return this.updateForm.get('methodOfJson');
  }
  public get u_dataFormat() {
    return this.updateForm.get('dataFormat');
  }
  // public get u_regHostId() {
  //   return this.updateForm.get('reghostId');
  // }
  public get u_apiDesc() {
    return this.updateForm.get('apiDesc');
  }

  public get apiCacheFlag() {
    return this.updateForm.get('apiCacheFlag')!;
  }
  public get u_mockStatusCode() {
    return this.updateForm.get('mockStatusCode')!;
  }
  public get u_mockHeaders() {
    return this.updateForm.get('mockHeaders')!;
  }
  public get u_mockBody() {
    return this.updateForm.get('mockBody')!;
  }
  // public get useDiversionSrcIP() { return this.updateForm.get('useDiversionSrcIP')!; };
  public get redirectByIp() {
    return this.updateForm.get('redirectByIp')!;
  }
  public get redirectByIpDataList() {
    return this.updateForm.get('redirectByIpDataList')!;
  }

  public get headerMaskPolicy() {
    return this.updateForm.get('headerMaskPolicy')!;
  }
  public get headerMaskPolicyNum() {
    return this.updateForm.get('headerMaskPolicyNum')!;
  }
  public get headerMaskPolicySymbol() {
    return this.updateForm.get('headerMaskPolicySymbol')!;
  }
  public get headerMaskKey() {
    return this.updateForm.get('headerMaskKey')!;
  }

  public get bodyMaskPolicy() {
    return this.updateForm.get('bodyMaskPolicy')!;
  }
  public get bodyMaskPolicyNum() {
    return this.updateForm.get('bodyMaskPolicyNum')!;
  }
  public get bodyMaskPolicySymbol() {
    return this.updateForm.get('bodyMaskPolicySymbol')!;
  }
  public get bodyMaskKeyword() {
    return this.updateForm.get('bodyMaskKeyword')!;
  }

  public get labelList() {
    return this.updateForm.get('labelList')!;
  }
  public get fixedCacheTime() {
    return this.updateForm.get('fixedCacheTime')!;
  }

  public get failDiscoveryPolicy() {
    return this.updateForm.get('failDiscoveryPolicy')!;
  }
  public get failHandlePolicy() {
    return this.updateForm.get('failHandlePolicy')!;
  }

  public get notifyNameList() {
    return this.updateForm.get('notifyNameList')!;
  }

  public get isCorsAllowOrigin() {
    return this.updateForm.get('isCorsAllowOrigin')!;
  }
  public get isCorsAllowMethods() {
    return this.updateForm.get('isCorsAllowMethods')!;
  }
  public get isCorsAllowHeaders() {
    return this.updateForm.get('isCorsAllowHeaders')!;
  }
  public get corsAllowOrigin() {
    return this.updateForm.get('corsAllowOrigin')!;
  }
  public get corsAllowMethods() {
    return this.updateForm.get('corsAllowMethods')!;
  }
  public get corsAllowHeaders() {
    return this.updateForm.get('corsAllowHeaders')!;
  }

  public get file() {
    return this.form.get('file')!;
  }
  public get fileName() {
    return this.form.get('fileName')!;
  }
  public get fileSize() {
    return this.form.get('fileSize')!;
  }
}

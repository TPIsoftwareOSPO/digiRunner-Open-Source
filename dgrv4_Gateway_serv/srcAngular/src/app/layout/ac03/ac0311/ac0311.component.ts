import { ValidatorFn } from '@angular/forms';
import { ServerService } from './../../../shared/services/api-server.service';
import { DPB9901Req } from './../../../models/api/ServerService/dpb9901.interface';
import { FileService } from 'src/app/shared/services/api-file.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA0315Item, AA0315Req, AA0315Resp } from 'src/app/models/api/ApiService/aa0315_v3.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { RegHostService } from 'src/app/shared/services/api-reg-host.service';
import { AA0806Req } from 'src/app/models/api/RegHostService/aa0806.interface';
import { AA0316Item, AA0316Req } from 'src/app/models/api/ApiService/aa0316_v3.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { CommonAPI } from 'src/app/shared/register-api/common-api.class';
import { KeyValueComponent } from 'src/app/shared/key-value/key-value.module';
import { AA0301Item, AA0301Req } from 'src/app/models/api/ApiService/aa0301_v3.interfcae';
import { AA0302Req, AA0302Resp } from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { AA0311Req } from 'src/app/models/api/ApiService/aa0311_v3.interface';
import { AA0312Req, AA0312Resp } from 'src/app/models/api/ApiService/aa0312_v3.interface';
import * as ValidatorFns from '../../../shared/validator-functions';
import { Subscription } from 'rxjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DialogService } from 'primeng/dynamicdialog';
import { NotifyListComponent } from 'src/app/shared/notify-list/notify-list.component';


@Component({
  selector: 'app-ac0311',
  templateUrl: './ac0311.component.html',
  styleUrls: ['./ac0311.component.css'],
  providers: [ApiService, MessageService, ConfirmationService]
})
export class Ac0311Component extends BaseComponent implements OnInit {

  @ViewChild('upload_file') upload_file!: ElementRef;
  @ViewChild('keyValueForm') kv_form!: KeyValueComponent;
  @ViewChild('keyValueRequest') kv_request!: KeyValueComponent;

  pageNum: number = 1; // 1：註冊、2：Copy API
  currentTitle: string = this.title;
  oasForm!: FormGroup;
  customForm!: FormGroup;
  testForm!: FormGroup;
  openApiCols: { field: string; header: string; }[] = [];
  openApiList: Array<AA0315Item> = [];
  openApiListRowcount: number = 0;
  tabList: { label: string; value: string; }[] = [];
  currentTabIdx: number = 0;
  openApiDocData?: AA0315Resp;
  regHostIdList: { label: string; value: string }[] = [];
  apiDataFormats: { label: string; value: string }[] = [];
  jwtSettingFlags: { label: string; value: string; }[] = [];
  rowDataIdx: number = 0;
  oasTab: boolean = true;
  customTab: boolean = false;
  httpmethods: { label: string; value: string; }[] = [];
  disabledReqBody: boolean = true;
  apiListCols: { field: string; header: string; }[] = [];
  apiForm!: FormGroup;
  apiList: Array<AA0301Item> = [];
  selectedApi?: AA0301Item;
  apiListRowcount: number = 0;
  protocols: { label: string; value: string; }[] = [];
  apiDetail?: AA0302Resp;
  testResult?: AA0312Resp;
  joinBtnFlag: boolean = true;
  showResult: boolean = false;
  showHostUrl: boolean = false; //OpenAPI Doc URL 回傳如出現error 1424 需填入hosturl再送出

  pathType: string = '';

  tarUrlSubscription = new Subscription();

  reqheaderExp: string = `{
    "Authorization":"Bearer xxxxxx.yyyyy.zzzz",
    "Authorization":"Basic xxxxxxxx",
    "Accept":"application/json",
    "Accept-encoding":"hzip, deflate,br"
}`;

  apiTypeList: { label: string; value: string }[] = [
    {
      label: 'Http API',
      value: 'HTTP_API'
    },
    {
      label: 'AI Gateway',
      value: 'AI_GATEWAY'
    },
    {
      label: 'Webhook',
      value: 'WEBHOOK'
    },
  ];

  jwtSettingSub?:Subscription;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private apiService: ApiService,
    private tool: ToolService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private list: ListService,
    private fileService: FileService,
    private regHostService: RegHostService,
    private ngxService: NgxUiLoaderService,
    private serverService: ServerService,
    private confirmationService: ConfirmationService,
    private alertService: AlertService,
      private dialogService: DialogService,
  ) {
    super(route, tr);
    this.oasForm = this.fb.group({
      apiSrc: new FormControl(''),
      moduleSrc: new FormControl(''),
      uploadDoc: new FormControl('0'),
      filename: new FormControl({ value: '', disabled: false }),
      docUrl: new FormControl({ value: '', disabled: true }),
      uploadfile: new FormControl(''),
      moduleName: new FormControl(''),
      regHostId: new FormControl(''),
      moduleVersion: new FormControl(''),
      regApiList: new FormControl([]),
      tempFileName: new FormControl(''),
      hostUrl: new FormControl(''),
      type: new FormControl(''),
      targetUrl: new FormControl(''),
    });
    this.customForm = this.fb.group({
      apiSrc: new FormControl(''),
      apiId: new FormControl(''),
      apiAlias: new FormControl(''),
      protocol: new FormControl('https'),
      srcUrl: new FormControl(''),
      moduleName: new FormControl(''),
      tsmpUrl: new FormControl(''),
      urlRID: new FormControl(false),
      noOAuth: new FormControl(false),
      tokenPayload: new FormControl(false),
      methods: new FormControl([]),
      dataFormat: new FormControl('1'),
      jwtSetting: new FormControl(false),
      jweFlag: new FormControl({ value: '0', disabled: true }),
      jweFlagResp: new FormControl({ value: '0', disabled: true }),
      // regHostId: new FormControl(''),
      apiDesc: new FormControl(''),
      type: new FormControl(''),
      apiName: new FormControl(''),
      dgrPath: new FormControl(''),
      redirectByIp: new FormControl(false),
      redirectByIpDataList: new FormControl([]),
      headerMaskPolicy: new FormControl(0), //; 0,1,2,3
      headerMaskPolicyNum: new FormControl(1), //; 1~9999
      headerMaskPolicySymbol: new FormControl('*'),//;   1 ~10
      headerMaskKey: new FormControl(''),//;  XXXX,XXXXXX,XXXXX
      // headerPolicy: new FormControl(0),
      // headerMaskCharNum: new FormControl(1),
      // headerMaskValue: new FormControl('*'),
      // fields: new FormControl(''),
      // maskBodyKeyword: new FormControl(''),
      bodyMaskPolicy: new FormControl(0), //; 0,1,2,3
      bodyMaskPolicyNum: new FormControl(1), //; 1~9999
      bodyMaskPolicySymbol: new FormControl('*'),//;   1 ~10
      bodyMaskKeyword: new FormControl(''),//;  XXXX,XXXXXX,XXXX
      labelList: new FormControl([]),
      failDiscoveryPolicy: new FormControl('0'),
      failHandlePolicy: new FormControl('0'),
      apiType: new FormControl('HTTP_API'),
      notifyNameList: new FormControl([]),
    });
    this.testForm = this.fb.group({
      method: new FormControl('POST'),
      testURL: new FormControl(''),
      basicAuth: new FormControl(false),
      userName: new FormControl({ value: '', disabled: true }),
      passwd: new FormControl({ value: '', disabled: true }),
      requestHeader: new FormControl(false),
      keyValueRequest: new FormControl({ value: '', disabled: true }),
      requestBodyCheck: new FormControl(false),
      requestBody: new FormControl({ value: '', disabled: true }),
      bodyText: new FormControl(''),
      keyValueForm: new FormControl({ value: '', disabled: true })
    });
    this.apiForm = this.fb.group({
      keyword: new FormControl('')
    });

    this.labelList.valueChanges.subscribe(res => {
      this.labelList.setValue(Array.isArray(res) ? res.map(item => item.toLowerCase()) : [], { emitEvent: false })
    })

    this.uploadDoc?.valueChanges.subscribe((res) => {
      this.showHostUrl = false;
      if (res == '0') {
        this.filename!.enable();
        this.docUrl!.disable();
        this.docUrl!.reset();
      } else {
        this.filename!.disable();
        this.docUrl!.enable();
        this.filename!.reset();
      }
    });

  }

  ngOnInit() {

    //mode
    let modeReq = {
      id: 'DGR_PATHS_COMPATIBILITY',
    } as DPB9901Req

    this.serverService.queryTsmpSettingDetail(modeReq).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.pathType = res.RespBody.value;
        this.oasForm.get("type")?.setValue(this.pathType == '2' ? '1' : this.pathType)
        this.customForm.get("type")?.setValue(this.pathType == '2' ? '1' : this.pathType)

      }
    })




    // http method
    this.httpmethods = CommonAPI.methods;
    // 匯入種類
    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('REG_SRC')) + ',' + 28,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _tabOpt: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          for (let item of res.RespBody.subItems) {
            _tabOpt.push({ label: item.subitemName, value: item.subitemNo });
          }
        }
        this.tabList = _tabOpt;
      }
    });
    // 資料格式
    let dataFormatReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_DATA_FORMAT')) + ',' + 30,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(dataFormatReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _apiDataFormats: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map(item => {
            _apiDataFormats.push({ label: item.subitemName, value: item.subitemNo });
          });
        }
        this.apiDataFormats = _apiDataFormats;
      }
    });
    // JWT設定
    let jwtSettingReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_JWT_FLAG')) + ',' + 31,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(jwtSettingReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _jwtSettings: { label: string, value: string }[] = [];
        if (res.RespBody.subItems) {
          res.RespBody.subItems.map(item => {
            _jwtSettings.push({ label: item.subitemName, value: item.subitemNo });
          });
        }
        this.jwtSettingFlags = _jwtSettings;
      }
    });
    // protocol
    this.protocols = [
      { label: 'https://', value: 'https' },
      { label: 'http://', value: 'http' }
    ];
    // 註冊主機清單
    let hostReqBody = {
      paging: 'false'
    } as AA0806Req;
    this.regHostService.queryRegHostList_1_ignore1298(hostReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        let _hostList: { label: string, value: string }[] = [];
        res.RespBody.hostInfoList.map(item => {
          _hostList.push({ label: item.regHost, value: item.regHostID });
        });
        this.regHostIdList = _hostList;
      }
    });
    this.c_moduleName!.valueChanges.subscribe(text => {
      if (text) {
        if (this.c_apiId!.value) {
          this.c_tsmpUrl!.setValue(`/${text}/${this.c_apiId!.value}`);
        }
        else {
          this.c_tsmpUrl!.setValue(`/${text}`);
        }
      }
      else {
        if (this.c_apiId!.value) {
          this.c_tsmpUrl!.setValue(`/${this.c_apiId!.value}`);
        }
        else {
          this.c_tsmpUrl!.setValue('');
        }
      }
    });
    this.c_apiId!.valueChanges.subscribe(text => {
      if (text) {
        if (this.c_moduleName!.value) {
          this.c_tsmpUrl!.setValue(`/${this.c_moduleName!.value}/${text}`);
        }
        else {
          this.c_tsmpUrl!.setValue(`/${text}`);
        }
      }
      else {
        if (this.c_moduleName!.value) {
          this.c_tsmpUrl!.setValue(`/${this.c_moduleName!.value}`);
        }
        else {
          this.c_tsmpUrl!.setValue('');
        }
      }
    });

    this.o_type.valueChanges.subscribe(() => {
      this.tarUrlSubscription.unsubscribe();
      this.openApiDocData = undefined;
    })

    this.init();

    this.headerMaskPolicyNum.valueChanges.subscribe(res => {
      if (Number(res) <= 0) {
        this.headerMaskPolicyNum.setValue(1)
      }
      else if (Number(res) > 9999) {
        this.headerMaskPolicyNum.setValue(9999)
      }
    })

    this.bodyMaskPolicyNum.valueChanges.subscribe(res => {
      if (Number(res) <= 0) {
        this.bodyMaskPolicyNum.setValue(1)
      }
      else if (Number(res) > 9999) {
        this.bodyMaskPolicyNum.setValue(9999)
      }
    })

    this.requestHeader?.valueChanges.subscribe(res=>{
      this.keyValueRequest!.reset();
    })

    this.requestBodyCheck?.valueChanges.subscribe(res=>{
      if (res) {
        this.requestBody!.enable();
      }
      else {
        this.requestBody!.setValue('none');
        this.requestBody!.disable();
      }
    })

    this.requestBody.valueChanges.subscribe(res=> {

      this.bodyText!.disable();
      this.bodyText!.reset();
      this.keyValueForm!.disable();
      this.keyValueForm!.reset();
      this.disabledReqBody = true;

      switch (res) {
        case 'body':
          this.bodyText!.enable();
          break;
        case 'form':
          this.disabledReqBody = false;
          this.keyValueForm!.enable();
          break;
        default:
          break;
      }
    })

    this.c_apiType.valueChanges.subscribe(res=> {
      this.clearCustomizePageData(false);
      if(res=='WEBHOOK'){
        this.srcUrl?.setValue('dgr+webhook##dgrv4/webhook')
        this.srcUrl?.disable();
      }
    })

  }

  async init() {
    const code = ['digirunner_url', 'http_method', 'src_complete_url', 'api_key', 'api_desc', 'api_name', 'module_name'];
    const dict = await this.tool.getDict(code);
    this.openApiCols = [
      { field: 'rearPath', header: dict['digirunner_url'] },
      { field: 'methods', header: dict['http_method'] },
      { field: 'srcUrl', header: dict['src_complete_url'] }
    ];
    // API清單的cols
    this.apiListCols = [
      { field: 'apiKey', header: dict['api_key'] },
      { field: 'moduleName', header: dict['module_name'] },
      { field: 'apiName', header: dict['api_name'] },
      { field: 'apiDesc', header: dict['api_desc'] }
    ];
  }

  //根據所選模式調整欄位名稱跟內容
  async setOpenApiCols() {
    const code = ['digirunner_url', 'http_method', 'src_complete_url', 'tar_complete_url'];
    const dict = await this.tool.getDict(code);
    if (this.o_type.value == '0')  //tsmpc
    {
      this.openApiCols = [
        { field: 'rearPath', header: dict['digirunner_url'] },
        { field: 'methods', header: dict['http_method'] },
        { field: 'srcUrl', header: dict['src_complete_url'] }
      ];
    }
    else  //dgrc
    {
      this.openApiCols = [
        { field: 'summary', header: 'Summary' },
        { field: 'methods', header: dict['http_method'] },
        { field: 'srcUrl', header: dict['tar_complete_url'] }
      ];
    }
  }

  async uploadClickHandler(uploadValue) {
    if (!uploadValue) return;
    if (typeof uploadValue == 'object') { // 透過 .json、.yml
      this.fileService.uploadFile2(uploadValue).subscribe(res => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.getOpenApiDocData(res.RespBody.tempFileName);
          this.tempFileName!.setValue(res.RespBody.tempFileName);
        }
      });
    }
    else if (typeof uploadValue == 'string') { // 透過URL
      let docFileData = await this.getUploadFile(uploadValue);
      this.fileService.uploadFile2(docFileData).subscribe(res => {
        if (this.tool.checkDpSuccess(res.ResHeader)) {
          this.getOpenApiDocData(res.RespBody.tempFileName);
          this.tempFileName!.setValue(res.RespBody.tempFileName);
        }
      });
    }
    else {
      return;
    }
  }

  getOpenApiDocData(tempFileName: string) {

    this.setOpenApiCols();

    // 清除資料
    if (this.openApiList) {
      this.openApiList.map((item, idx) => {
        this.oasForm.removeControl(`apiDesc_${idx}`);
        this.oasForm.removeControl(`dataFormat_${idx}`);
        this.oasForm.removeControl(`tokenPayload_${idx}`);
        this.oasForm.removeControl(`noOAuth_${idx}`);
        this.oasForm.removeControl(`urlRID_${idx}`);
        this.oasForm.removeControl(`jweFlagResp_${idx}`);
        this.oasForm.removeControl(`jweFlag_${idx}`);
        this.oasForm.removeControl(`jwtSetting_${idx}`);
        this.oasForm.removeControl(`apiId_${idx}`);
        this.oasForm.removeControl(`srcUrl_${idx}`);
        this.oasForm.removeControl(`summary_${idx}`);
      });
    }
    // this.openApiDocData = {} as AA0315Resp;
    this.openApiList = [];
    this.openApiListRowcount = this.openApiList.length;
    this.ngxService.start();
    let ReqBody = {
      tempFileName: tempFileName,
      type: this.o_type.value
    } as AA0315Req;

    if (this.showHostUrl) {
      ReqBody.optionHost = this.hostUrl!.value;
    }

    const _this = this;

    this.apiService.uploadOpenApiDoc_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.showHostUrl = false;
        this.ngxService.stop();
        this.openApiDocData = res.RespBody;
        this.o_apiSrc!.setValue('R');
        this.o_moduleSrc!.setValue(this.openApiDocData.moduleSrc);
        this.o_moduleName!.setValue(this.openApiDocData.moduleName);
        this.o_moduleVersion!.setValue(this.openApiDocData.moduleVersion);
        this.o_regApiList!.setValue(this.openApiDocData.openApiList);
        this.openApiList = res.RespBody.openApiList;
        this.openApiListRowcount = this.openApiList.length;


        const tmpTargetUrl = this.o_type.value == '0' ? (this.openApiDocData.protocol + this.openApiDocData.host) : (this.openApiDocData.protocol + this.openApiDocData.host + this.openApiDocData.basePath);
        this.o_tarUrl.setValue(tmpTargetUrl);



        // let checkDuplicates = [];
        this.openApiList.map((item, idx) => {

          // if (checkDuplicates.indexOf(item.rearPath) == -1) {
          //     checkDuplicates.push(item.rearPath)
          // }
          // else {
          //     item.duplicateFlag = true;
          // }
          // 動態產生各個rowdata的jwe設定相關control name
          this.oasForm.addControl(`jwtSetting_${idx}`, new FormControl(false));
          this.oasForm.addControl(`jweFlag_${idx}`, new FormControl({ value: '0', disabled: true }));
          this.oasForm.addControl(`jweFlagResp_${idx}`, new FormControl({ value: '0', disabled: true }));
          // 動態產生各個rowdata的功能設定相關control name
          this.oasForm.addControl(`urlRID_${idx}`, new FormControl(false));
          this.oasForm.addControl(`noOAuth_${idx}`, new FormControl(false));
          this.oasForm.addControl(`tokenPayload_${idx}`, new FormControl(false));
          // 動態產生各個rowdata的資料格式control name
          this.oasForm.addControl(`dataFormat_${idx}`, new FormControl(''));
          if (this.openApiDocData?.moduleSrc == '1') {
            this.oasForm.get(`dataFormat_${idx}`)!.setValue('0');
          }
          else {
            this.oasForm.get(`dataFormat_${idx}`)!.setValue('1');
          }
          // 動態產生各個rowdata的API說明control name
          this.oasForm.addControl(`apiDesc_${idx}`, new FormControl(''));
          // 動態產生各個rowdata的API ID control name
          this.oasForm.addControl(`apiId_${idx}`, new FormControl(item.rearPath));
          if (this.o_type.value == '0') {
            this.oasForm.get(`apiId_${idx}`)?.valueChanges.subscribe(res => {
              // console.log(item.rearPath)
              item.rearPath = res;

            })
          }
          // 動態產生各個rowdata的API來源URLcontrol name
          if (this.o_type.value == '0') {
            this.oasForm.addControl(`srcUrl_${idx}`, new FormControl(item.srcUrl));
          }
          else {
            const compTarURl = this.o_tarUrl.value.charAt(this.o_tarUrl.value.length - 1) == '/' ? this.o_tarUrl.value.substr(0, this.o_tarUrl.value.length - 1) + item.path : this.o_tarUrl.value + item.path
            this.oasForm.addControl(`srcUrl_${idx}`, new FormControl(compTarURl));
          }

          this.oasForm.get(`srcUrl_${idx}`)?.valueChanges.subscribe(res => {
            item.srcUrl = res;
          })

          this.oasForm.addControl(`summary_${idx}`, new FormControl(item.summary));
          if (this.o_type.value == '1') {
            this.oasForm.get(`summary_${idx}`)?.valueChanges.subscribe(res => {
              item.summary = res;
            })
          }

          if (this.o_type.value == '0' && (item.srcUrl.indexOf('{') > -1 || item.srcUrl.indexOf('}') > -1)) {
            item.duplicateFlag = true;
          }
        });




        this.tarUrlSubscription = this.o_tarUrl.valueChanges.subscribe(() => {
          const formatTarUrl = this.o_type.value == '0' ? this.o_tarUrl.value + this.openApiDocData?.basePath : this.o_tarUrl.value
          this.openApiList.map((item, idx) => {
            const compTarURl = formatTarUrl.charAt(formatTarUrl.length - 1) == '/' ? formatTarUrl.substr(0, formatTarUrl.length - 1) + item.path : formatTarUrl + item.path
            this.oasForm.get(`srcUrl_${idx}`)?.setValue(compTarURl);
            item.srcUrl = compTarURl;
          })
        })


        const _this = this
        const multiList = this.openApiList.filter((item, index) => {
          return _this.openApiList.some((multi, idx) => item.rearPath === multi.rearPath && idx !== index)
        })

        multiList.forEach(item => {
          item.duplicateFlag = true
        })



        this.apiService.registerAPIList_v3_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.oasForm, res.RespBody.constraints);
          }
        });
        this.o_moduleName!.markAsTouched();
        if (this.o_type.value == '1') this.o_tarUrl.markAsTouched();
      }
      else {
        if (res.ResHeader.rtnCode == '1424') {
          this.showHostUrl = true;
        }
      }
    });
  }

  getUploadFile(url) {
    return new Promise<File>((resolve, reject) => {
      $.get(url, (data) => {
        //加工
        let filename = this.getFilename(url);
        let jsonse = JSON.stringify(data);
        let blob = new Blob([jsonse], { type: "application/json" });
        let file = new File([blob], filename);
        resolve(file);
      })
    })
  }

  getFilename(url) {
    url = url.split('/').pop().replace(/\#(.*?)$/, '').replace(/\?(.*?)$/, '');
    url = url.split('.');  // separates filename and extension
    return url += '.json';
  }

  async registerOpenApi() {


    // this.ngxService.start();
    const codes = ['message.reg_api', 'message.register', 'message.success']
    const dict = await this.tool.getDict(codes);
    let ReqBody = {
      apiSrc: 'R',
      moduleSrc: this.openApiDocData!.moduleSrc,
      tempFileName: this.tempFileName!.value,
      moduleName: this.o_moduleName!.value,
      moduleVersion: this.openApiDocData!.moduleVersion,
      regHostId: this.o_regHostId!.value,
      type: this.openApiDocData?.type,
      targetUrl: this.o_tarUrl.value
    } as AA0316Req;
    let _regApiList = Array<AA0316Item>();
    this.openApiList.map((item, idx) => {
      item.rearPath = this.oasForm.get(`apiId_${idx}`)!.value;
      _regApiList.push({
        methods: item.methods,
        srcUrl: this.oasForm.get(`srcUrl_${idx}`)!.value,
        apiId: this.oasForm.get(`apiId_${idx}`)!.value,
        dataFormat: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.oasForm.get(`dataFormat_${idx}`)!.value)) + ',' + this.apiDataFormats.findIndex(item => item.value == this.oasForm.get(`dataFormat_${idx}`)!.value),
        apiDesc: this.oasForm.get(`apiDesc_${idx}`)!.value,
        jweFlag: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.oasForm.get(`jweFlag_${idx}`)!.value)) + ',' + this.jwtSettingFlags.findIndex(item => item.value == this.oasForm.get(`jweFlag_${idx}`)!.value),
        jweFlagResp: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.oasForm.get(`jweFlagResp_${idx}`)!.value)) + ',' + this.jwtSettingFlags.findIndex(item => item.value == this.oasForm.get(`jweFlagResp_${idx}`)!.value),
        urlRID: this.oasForm.get(`urlRID_${idx}`)!.value,
        noOAuth: this.oasForm.get(`noOAuth_${idx}`)!.value,
        funFlag: {
          tokenPayload: this.oasForm.get(`tokenPayload_${idx}`)!.value
        },
        consumes: item.consumes,
        produces: item.produces,
        headers: item.headers,
        params: item.params,
        moduleName: item.moduleName,
        summary: item.summary
      }
      );

    });
    ReqBody.regApiList = _regApiList;
    this.setDuplicateData();

    this.ngxService.start();
    this.apiService.registerAPIList_v3(ReqBody).subscribe(res => {
      this.ngxService.stop();
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        // this.openApiDocData = Object.assign({}) as AA0315Resp;
        this.messageService.add({ severity: 'success', summary: dict['message.reg_api'], detail: `${dict['message.register']} ${dict['message.success']}!` });
      }
      this.ngxService.stop();
    });
  };

  callUploadDialogHandler(evt) {
    this.upload_file.nativeElement.click();
  }

  fileChangeHandler(evt) {
    const filename = this.uploadfile!.value ? (this.uploadfile!.value as File).name : '';
    this.filename!.setValue(filename);
  }

  uploadDocChangeHandler(evt) {

    this.showHostUrl = false;
    if (this.uploadDoc!.value == '0') {
      this.filename!.enable();
      this.docUrl!.disable();
      this.docUrl!.reset();
    } else {
      this.filename!.disable();
      this.docUrl!.enable();
      this.filename!.reset();
    }
  }

  // 控制table展開 OR 收合
  openTableAllExpand() {
    for (let i = 0; i < this.openApiListRowcount; i++) {
      document.getElementById(`expanded_${i}`)!.style.display = '';
    }
  }

  closeTableAllExpand() {
    for (let i = 0; i < this.openApiListRowcount; i++) {
      document.getElementById(`expanded_${i}`)!.style.display = 'none';
    }
  }

  openTableExpandByIndex(idx: number) {
    document.getElementById(`expanded_${idx}`)!.style.display = '';
  }

  closeTableExpandByIndex(idx: number) {
    document.getElementById(`expanded_${idx}`)!.style.display = 'none';
  }

  jwtSettingClick(evt,idx:number){
    if (this.oasForm.get(`jwtSetting_${idx}`)!.value) {
          this.oasForm.get(`jweFlag_${idx}`)!.enable();
          this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlagResp_${idx}`)!.enable();
          this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('1');
    }
    else {
      this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
      this.oasForm.get(`jweFlag_${idx}`)!.disable();
      this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('0');
      this.oasForm.get(`jweFlagResp_${idx}`)!.disable();
    }
  }

  // 控制rowData的jwe setting
  jwtSettingOnChange(evn, idx?: number) {
    switch (this.currentTabIdx) {
      case 0:
        if (evn.checked == true) {
          this.oasForm.get(`jweFlag_${idx}`)!.enable();
          this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlagResp_${idx}`)!.enable();
          this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('1');
        }
        else {
          this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlag_${idx}`)!.disable();
          this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlagResp_${idx}`)!.disable();
        }
        break;
      case 1:
        if (evn.checked == true) {
          this.c_jweFlag!.enable();
          this.c_jweFlag!.setValue('0');
          this.c_jweFlagResp!.enable();
          this.c_jweFlagResp!.setValue('1');
        }
        else {
          this.c_jweFlag!.setValue('0');
          this.c_jweFlag!.disable();
          this.c_jweFlagResp!.setValue('0');
          this.c_jweFlagResp!.disable();
        }
        break;
    }
  }

  // this.jwtSettingSub = this.u_jwtSetting?.valueChanges.subscribe(
  //               (states) => {
  //                 // console.log(states);
  //                 if (states) {
  //                   this.u_jweFlag!.enable();
  //                   this.u_jweFlag!.setValue('0');
  //                   this.u_jweFlagResp!.enable();
  //                   this.u_jweFlagResp!.setValue('1');
  //                 } else {
  //                   this.u_jweFlag!.setValue('0');
  //                   this.u_jweFlag!.disable();
  //                   this.u_jweFlagResp!.setValue('0');
  //                   this.u_jweFlagResp!.disable();
  //                 }
  //               }
  //             );

  jweFlagOnChange(evn, idx?: number) {
    switch (this.currentTabIdx) {
      case 0:
        if (evn.value == '0' && this.oasForm.get(`jweFlagResp_${idx}`)!.value == '0') {
          this.oasForm.get(`jwtSetting_${idx}`)!.setValue(false);
          this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlag_${idx}`)!.disable();
          this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlagResp_${idx}`)!.disable();
        }
        break;
      case 1:
        if (evn.value == '0' && this.c_jweFlagResp!.value == '0') {
          this.c_jwtSetting!.setValue(false);
          this.c_jweFlag!.setValue('0');
          this.c_jweFlag!.disable();
          this.c_jweFlagResp!.setValue('0');
          this.c_jweFlagResp!.disable();
        }
        break;
    }
  }

  jweFlagRespOnChange(evn, idx?: number) {
    switch (this.currentTabIdx) {
      case 0:
        if (evn.value == '0' && this.oasForm.get(`jweFlag_${idx}`)!.value == '0') {
          this.oasForm.get(`jwtSetting_${idx}`)!.setValue(false);
          this.oasForm.get(`jweFlag_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlag_${idx}`)!.disable();
          this.oasForm.get(`jweFlagResp_${idx}`)!.setValue('0');
          this.oasForm.get(`jweFlagResp_${idx}`)!.disable();
        }
        break;
      case 1:
        if (evn.value == '0' && this.c_jweFlag!.value == '0') {
          this.c_jwtSetting!.setValue(false);
          this.c_jweFlag!.setValue('0');
          this.c_jweFlag!.disable();
          this.c_jweFlagResp!.setValue('0');
          this.c_jweFlagResp!.disable();
        }
        break;
    }
  }

  // 全部套用設定
  async cloneConfirm(idx: number) {
    this.rowDataIdx = idx;
    const code = ['cfm_oas_setting'];
    const dict = await this.tool.getDict(code);
    // this.messageService.add({ key: 'cloneSetting', sticky: true, severity: 'warn', summary: dict['cfm_oas_setting'] });

    this.confirmationService.confirm({
      header: ' ',
      message: dict['cfm_oas_setting'],
      accept: () => {
        this.cloneAllSetting()
      }
    });


  }

  cloneAllSetting() {
    this.messageService.clear();
    // 當前JWT設定值
    let _jwtSetting = this.oasForm.get(`jwtSetting_${this.rowDataIdx}`)!.value;
    let _jweFlag = this.oasForm.get(`jweFlag_${this.rowDataIdx}`)!.value;
    let _jweFlagResp = this.oasForm.get(`jweFlagResp_${this.rowDataIdx}`)!.value;
    // 當前資料格式值
    let _dataFormat = this.oasForm.get(`dataFormat_${this.rowDataIdx}`)!.value;
    // 當前API說明值
    let _apiDesc = this.oasForm.get(`apiDesc_${this.rowDataIdx}`)!.value;
    // 當前功能設定值
    let _urlRID = this.oasForm.get(`urlRID_${this.rowDataIdx}`)!.value;
    let _noOAuth = this.oasForm.get(`noOAuth_${this.rowDataIdx}`)!.value;
    let _tokenPayload = this.oasForm.get(`tokenPayload_${this.rowDataIdx}`)!.value;
    // 當前API ID
    let _apiId = this.oasForm.get(`apiId_${this.rowDataIdx}`)!.value;
    // 當前API來源URL
    let _srcUrl = this.oasForm.get(`srcUrl_${this.rowDataIdx}`)!.value;
    this.openApiList.map((item, idx) => {
      // 套用所有JWT設定
      this.oasForm.get(`jwtSetting_${idx}`)!.setValue(_jwtSetting);
      this.oasForm.get(`jweFlag_${idx}`)!.setValue(_jweFlag);
      this.oasForm.get(`jweFlagResp_${idx}`)!.setValue(_jweFlagResp);
      if (this.oasForm.get(`jwtSetting_${idx}`)!.value == true) {
        this.oasForm.get(`jweFlag_${idx}`)!.enable();
        this.oasForm.get(`jweFlagResp_${idx}`)!.enable();
      }
      else {
        this.oasForm.get(`jweFlag_${idx}`)!.disable();
        this.oasForm.get(`jweFlagResp_${idx}`)!.disable();
      }
      // 套用所有資料格式
      this.oasForm.get(`dataFormat_${idx}`)!.setValue(_dataFormat);
      // 套用所有API說明
      this.oasForm.get(`apiDesc_${idx}`)!.setValue(_apiDesc);
      // 套用所有功能設定
      this.oasForm.get(`urlRID_${idx}`)!.setValue(_urlRID);
      this.oasForm.get(`noOAuth_${idx}`)!.setValue(_noOAuth);
      this.oasForm.get(`tokenPayload_${idx}`)!.setValue(_tokenPayload);
      // 套用所有API ID
      this.oasForm.get(`apiId_${idx}`)!.setValue(_apiId);
      // 套用所有API來源URL
      this.oasForm.get(`srcUrl_${idx}`)!.setValue(_srcUrl);
      this.openTableExpandByIndex(idx)
    });
  }

  onReject() {
    this.messageService.clear();
  }

  // 基本驗證checkbox
  basicAuthChangeHandler(evt) {
    if (evt.target.checked) {
      this.userName!.enable();
      this.passwd!.enable();
    } else {
      this.userName!.disable();
      this.userName!.reset();
      this.passwd!.disable();
      this.passwd!.reset();
    }
  }

  // 請求表頭checkbox
  // requestHeaderChangeHandler(evt) {
  //   this.keyValueRequest!.reset();
  //   if (evt.target.checked) {
  //     this.keyValueRequest!.enable();
  //   } else {
  //     this.keyValueRequest!.disable();
  //   }
  // }

  // 增加一列header、body
  add(componentName: string) {
    componentName === 'request' ? this.kv_request.add() : this.kv_form.add();
  }

  // 請求表身checkbox
  // requestBodyCheckChangeHandler(evt) {
  //   if (this.requestBodyCheck!.value == false) {
  //     this.requestBody!.setValue('none');
  //     this.requestBody!.disable();
  //   }
  //   else {
  //     this.requestBody!.enable();
  //   }
  // }

  // 請求表身radio box
  // requestBodyChangeHandler(evt) {

  //   this.bodyText!.disable();
  //   this.bodyText!.reset();
  //   this.keyValueForm!.disable();
  //   this.keyValueForm!.reset();
  //   this.disabledReqBody = true;

  //   switch (evt.target.value) {
  //     case 'body':
  //       this.bodyText!.enable();
  //       break;
  //     case 'form':
  //       this.disabledReqBody = false;
  //       this.keyValueForm!.enable();
  //       break;
  //     default:
  //       break;
  //   }
  // }

  // 查詢API List查詢API List
  queryApiList() {
    this.apiList = [];
    this.apiListRowcount = this.apiList.length;
    this.selectedApi = {} as AA0301Item;
    let ReqBody = {
      keyword: this.keyword!.value,
      apiSrc: ['R'],
      paging: 'Y'
    } as AA0301Req;
    this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apiList = res.RespBody.dataList;
        this.apiListRowcount = this.apiList.length;
      }
    });
  }

  moreApiList() {
    let _moduleName = this.apiList[this.apiList.length - 1].moduleName;
    let _apiKey = this.apiList[this.apiList.length - 1].apiKey;
    let ReqBody = {
      moduleName: _moduleName.t ? _moduleName.ori : _moduleName.val,
      apiKey: _apiKey.t ? _apiKey.ori : _apiKey.val,
      keyword: this.keyword!.value,
      apiSrc: ['R'],
      paging: 'Y'
    } as AA0301Req;
    this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apiList = this.apiList.concat(res.RespBody.dataList);
        this.apiListRowcount = this.apiList.length;
      }
    });
  }

  // 選擇要複製的API
  chooseApi() {
    this.changePage('regist');
    let _moduleName = this.selectedApi!.moduleName;
    let _apiName = this.selectedApi!.apiName;
    let _apiKey = this.selectedApi!.apiKey;
    this.apiAlias!.setValue(`${_moduleName.t ? _moduleName.ori : _moduleName.val}-${_apiName.t ? _apiName.ori : _apiName.val}(${_apiKey.t ? _apiKey.ori : _apiKey.val})`);
  }

  // 複製API
  copyApiDetail() {
    this.apiDetail = {} as AA0302Resp;
    this.clearCustomizePageData();
    let _moduleName = this.selectedApi!.moduleName;
    let _apiKey = this.selectedApi!.apiKey;
    let ReqBody = {
      moduleName: _moduleName.t ? _moduleName.ori : _moduleName.val,
      apiKey: _apiKey.t ? _apiKey.ori : _apiKey.val
    } as AA0302Req;
    this.apiService.queryAPIDetail_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apiDetail = res.RespBody;
        // console.log('0302 :', this.apiDetail)
        // 設定API Detail資料
        // if (res.RespBody.protocol) {
        //   let _protocol = res.RespBody.protocol.split('://')[0];
        //   this.protocol!.setValue(_protocol);
        // }
        // if (res.RespBody.srcUrl?.t) {
        //   if (res.RespBody.srcUrl.o?.includes('://')) {
        //     this.srcUrl!.setValue(res.RespBody.srcUrl.o.split('://')[1]);
        //   }
        //   else {
        //     this.srcUrl!.setValue(res.RespBody.srcUrl.o);
        //   }
        // }
        // else {
        //   if (res.RespBody.srcUrl?.v.includes('://')) {
        //     this.srcUrl!.setValue(res.RespBody.srcUrl.v.split('://')[1]);
        //   }
        //   else {
        //     this.srcUrl!.setValue(res.RespBody.srcUrl?.v);
        //   }
        // }
        this.srcUrl!.setValue(res.RespBody.srcUrl?.o ? res.RespBody.srcUrl?.o : res.RespBody.srcUrl?.v);

        this.c_moduleName!.setValue(res.RespBody.moduleName.t ? res.RespBody.moduleName.o : res.RespBody.moduleName.v);
        this.c_apiId!.setValue(res.RespBody.apiKey.t ? res.RespBody.apiKey.o : res.RespBody.apiKey.v);
        this.c_tsmpUrl!.setValue(`/${this.c_moduleName!.value}/${this.c_apiId!.value}`);
        this.c_urlRID!.setValue(res.RespBody.urlRID == '1' ? true : false);
        this.c_noOAuth!.setValue(res.RespBody.noOAuth == '1' ? true : false);
        this.c_tokenPayload!.setValue(res.RespBody.funFlag == 1 ? true : false);
        let _methods = res.RespBody.methodOfJson!.toLocaleUpperCase().split(',');
        this.c_methods!.setValue(_methods);
        this.c_dataFormat!.setValue(res.RespBody.dataFormat.v);
        this.c_jweFlag!.setValue(res.RespBody.jweFlag.v);
        this.c_jweFlagResp!.setValue(res.RespBody.jweFlagResp.v);
        // JWT設定開關
        if ((this.c_jweFlag!.value == '0' || this.c_jweFlag!.value == null) && (this.c_jweFlagResp!.value == '0' || this.c_jweFlagResp!.value == null)) {
          this.c_jwtSetting!.setValue(false);
        }
        else {
          this.c_jwtSetting!.setValue(true);
        }
        // Requset設定
        if ((this.c_jweFlag!.value == '0' || this.c_jweFlag!.value == null) && (this.c_jweFlagResp!.value == '0' || this.c_jweFlagResp!.value == null)) {
          this.c_jweFlag!.disable();
        }
        else {
          this.c_jweFlag!.enable();
        }
        // Response設定
        if ((this.c_jweFlag!.value == '0' || this.c_jweFlag!.value == null) && (this.c_jweFlagResp!.value == '0' || this.c_jweFlagResp!.value == null)) {
          this.c_jweFlagResp!.disable();
        }
        else {
          this.c_jweFlagResp!.enable();
        }
        // this.c_regHostId!.setValue(res.RespBody.regHostId);
        this.c_apiDesc!.setValue(res.RespBody.apiDesc);

        if (this.c_type.value == '1') {
          this.c_apiName.setValue(res.RespBody.apiName.o ? res.RespBody.apiName.o : res.RespBody.apiName.v);
          this.c_dgrPath.setValue(res.RespBody.apiKey.o ? res.RespBody.apiKey.o : res.RespBody.apiKey.v);
        }

        this.redirectByIp.setValue(res.RespBody.isRedirectByIp);
        this.redirectByIpDataList.setValue(res.RespBody.redirectByIpDataList ? res.RespBody.redirectByIpDataList.map(row => {
          return {
            ipForRedirect: row.ipForRedirect,
            ipSrcUrl: row.ipSrcUrl.t ? row.ipSrcUrl.o : row.ipSrcUrl.v
          };
        }) : [])
        this.headerMaskKey.setValue(res.RespBody.headerMaskKey)
        this.headerMaskPolicy.setValue(res.RespBody.headerMaskPolicy);
        this.headerMaskPolicyNum.setValue(res.RespBody.headerMaskPolicyNum);
        this.headerMaskPolicySymbol.setValue(res.RespBody.headerMaskPolicySymbol);

        this.bodyMaskKeyword.setValue(res.RespBody.bodyMaskKeyword);
        this.bodyMaskPolicy.setValue(res.RespBody.bodyMaskPolicy);
        this.bodyMaskPolicyNum.setValue(res.RespBody.bodyMaskPolicyNum);
        this.bodyMaskPolicySymbol.setValue(res.RespBody.bodyMaskPolicySymbol);

        this.labelList.setValue(res.RespBody.labelList);
      }
    });
  }

  testAPI() {
    this.ngxService.start();
    let ReqBody = {
      testURL: this.t_testURL!.value,
      method: this.t_method!.value,
    } as AA0312Req;
    if (this.basicAuth!.value) {
      let encoder = btoa(this.userName!.value + ':' + this.passwd!.value)
      let headerAuth = [{
        key: 'Authorization',
        value: `Basic ${encoder}`
      }];
      ReqBody.headerList = this.keyvalueConvetToList(headerAuth);
    }
    if (this.requestHeader!.value) {
      if (ReqBody.headerList && ReqBody.headerList.length > 0) {
        ReqBody.headerList = ReqBody.headerList.concat(this.keyvalueConvetToList(this.keyValueRequest!.value));
      }
      else {
        ReqBody.headerList = this.keyvalueConvetToList(this.keyValueRequest!.value);
      }
    }
    if (this.requestBodyCheck!.value) {
      switch (this.requestBody!.value) {
        case 'body':
          ReqBody.bodyText = this.bodyText!.value;
          break;
        case 'form':
          ReqBody.paramList = this.keyvalueConvetToList(this.keyValueForm!.value);
          break;
      }
    }
    // console.log('AA0312 Req :', ReqBody)
    this.apiService.testAPI_v3(ReqBody).subscribe(res => {
      this.ngxService.stop();
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.testResult = res.RespBody;
        this.showResult = true;
      }
    });
  }

  keyvalueConvetToList(keyvalues: { key: string, value: string, selected?: boolean }[]) {
    let keyValueList: Array<object> = new Array<object>();
    if (keyvalues && keyvalues.length) {
      (keyvalues.filter(item => {
        return item.selected;
      })).map(keyvalue => {
        let obj = {};
        obj[keyvalue.key] = keyvalue.value;
        keyValueList.push(obj);
      });
    }
    return keyValueList;
  }

  async registerCustomApi() {

    // this.ngxService.start();
    let reqBody = {
      apiSrc: 'R',
      protocol: this.protocol!.value,
      srcUrl: this.srcUrl!.value,
      moduleName: this.c_type.value == '0' ? this.c_moduleName!.value : this.c_dgrPath.value,
      apiId: this.c_type.value == '0' ? this.c_apiId!.value : this.c_dgrPath.value,
      urlRID: this.c_type.value == '0' ? this.c_urlRID!.value : false,
      noOAuth: this.c_noOAuth!.value,
      funFlag: {
        tokenPayload: this.c_tokenPayload!.value
      },
      methods: this.c_methods!.value,
      dataFormat: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.c_dataFormat!.value)) + ',' + this.apiDataFormats.findIndex(item => item.value == this.c_dataFormat!.value),
      // regHostId: this.c_regHostId!.value,
      jweFlag: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.c_jweFlag!.value)) + ',' + this.jwtSettingFlags.findIndex(item => item.value == this.c_jweFlag!.value),
      jweFlagResp: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.c_jweFlagResp!.value)) + ',' + this.jwtSettingFlags.findIndex(item => item.value == this.c_jweFlagResp!.value),
      apiDesc: this.c_apiDesc!.value,
      type: this.c_type.value,
    } as AA0311Req;



    if (this.c_type?.value == '1') {
      reqBody.apiName = this.c_apiName.value;
      reqBody.failDiscoveryPolicy = this.failDiscoveryPolicy.value;
      reqBody.failHandlePolicy = this.failHandlePolicy.value;
      reqBody.notifyNameList = this.c_notifyNameList.value
    }

    if (this.apiDetail && JSON.stringify(this.apiDetail) != '{}') {
      reqBody.consumes = this.apiDetail.consumesOfJson!.t ? JSON.parse(this.apiDetail.consumesOfJson?.o ?? '') : JSON.parse(this.apiDetail.consumesOfJson!.v);
      reqBody.produces = this.apiDetail.producesOfJson!.t ? JSON.parse(this.apiDetail.producesOfJson?.o ?? '') : JSON.parse(this.apiDetail.producesOfJson!.v);
      reqBody.headers = this.apiDetail.headersOfJson!.t ? JSON.parse(this.apiDetail.headersOfJson?.o ?? '') : JSON.parse(this.apiDetail.headersOfJson!.v);
      reqBody.params = this.apiDetail.paramsOfJson!.t ? JSON.parse(this.apiDetail.paramsOfJson?.o ?? '') : JSON.parse(this.apiDetail.paramsOfJson!.v);
    }
    // console.log('AA0311 Req :', ReqBody)

    //SourceIp分流 20231103
    // console.log(this.redirectByIp.value)
    reqBody.redirectByIp = this.redirectByIp.value;
    if (reqBody.redirectByIp) {

      //檢核***
      this.redirectByIpDataList.value.forEach(async item => {
        // console.log(item)
        const code = ['sourceIpRequired', 'ipSrcUrlRequired'];
        const dict = await this.tool.getDict(code);
        if (item.ipForRedirect == '') {
          this.alertService.ok(dict['sourceIpRequired'], '');
          return;
        }
        if (item.ipSrcUrl == '') {
          this.alertService.ok(dict['ipSrcUrlRequired'], '');
          return;
        }
      })
      reqBody.redirectByIpDataList = this.redirectByIpDataList.value;
    }

    reqBody.headerMaskPolicy = this.headerMaskPolicy.value;
    if (reqBody.headerMaskPolicy != '0') {
      reqBody.headerMaskPolicyNum = this.headerMaskPolicyNum.value;
      reqBody.headerMaskPolicySymbol = this.headerMaskPolicySymbol.value;


      if (this.headerMaskKey.value.length == 0) {
        const code = ['mask.header_key_required'];
        const dict = await this.tool.getDict(code);
        this.alertService.ok(dict['mask.header_key_required'], '');
        return;
      }
      // else{
      //   this.headerMaskKey.value.forEach(async item => {
      //     console.log(item)
      //     if(item.trim() == '') {
      //       const code = ['mask.header_key_required'];
      //       const dict = await this.tool.getDict(code);
      //       this.alertService.ok(dict['mask.header_key_required'], '');
      //       return;
      //     }
      //   });
      // }


      reqBody.headerMaskKey = this.headerMaskKey.value;//.join(',');

    }


    reqBody.bodyMaskPolicy = this.bodyMaskPolicy.value;
    if (reqBody.bodyMaskPolicy != '0') {
      reqBody.bodyMaskPolicyNum = this.bodyMaskPolicyNum.value;
      reqBody.bodyMaskPolicySymbol = this.bodyMaskPolicySymbol.value;

      if (this.bodyMaskKeyword.value.trim() == '') {
        const code = ['mask.body_key_required'];
        const dict = await this.tool.getDict(code);
        this.alertService.ok(dict['mask.body_key_required'], '');
        return;
      }
      reqBody.bodyMaskKeyword = this.bodyMaskKeyword.value;
    }

    reqBody.labelList = this.labelList.value;

    // console.log(reqBody);


    this.apiService.registerAPI_v3(reqBody).subscribe(async res => {
      this.ngxService.stop();
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const codes = ['message.reg_api', 'message.register', 'message.success'];
        const dict = await this.tool.getDict(codes);
        this.messageService.add({ severity: 'success', summary: dict['message.reg_api'], detail: `${dict['message.register']} ${dict['message.success']}!` });
        this.clearCustomizePageData();
      }
    });
  }

  // 切換tabview
  changeTab(evn) {
    this.currentTabIdx = evn.index;

    switch (evn.index) {
      case 0: // 匯入OAS 3.0
        this.oasForm.reset();
        this.openApiDocData = {} as AA0315Resp;
        this.openApiList = [];
        this.openApiListRowcount = this.openApiList.length;
        this.uploadDoc!.setValue('0');
        this.filename!.reset();
        this.filename!.enable();
        this.docUrl!.setValue('');
        this.docUrl!.disable();
        this.uploadfile!.setValue('');
        this.o_regHostId!.setValue(null);
        this.o_regApiList!.setValue([]);
        this.hostUrl!.setValue('');

        this.o_type.setValue(this.pathType == '2' ? '1' : this.pathType)

        if (this.c_type_sub) this.c_type_sub.unsubscribe();
        break;
      case 1: // customize自訂API
        // this.apiService.registerAPI_v3_before().subscribe(res => {
        //   if (this.tool.checkDpSuccess(res.ResHeader)) {
        //     this.addFormValidator(this.customForm, res.RespBody.constraints);
        //     this.clearCustomizePageData();
        //   }
        // });
        this.setCustomizePageValidate();



        // this.c_type.valueChanges.subscribe(()=>{



        // if(this.c_type.value == '0')
        // {
        //   this.c_moduleName?.addValidators(ValidatorFns.requiredValidator())
        //   this.c_apiId?.addValidators(ValidatorFns.requiredValidator())

        //   this.c_dgrPath.removeValidators(ValidatorFns.requiredValidator())
        //   this.c_apiName.removeValidators(ValidatorFns.requiredValidator())
        // }
        // else{
        //   this.c_moduleName?.removeValidators(ValidatorFns.requiredValidator());
        //   this.c_apiId?.removeValidators(ValidatorFns.requiredValidator())

        //   this.c_dgrPath.addValidators(ValidatorFns.requiredValidator())
        //   this.c_apiName.addValidators(ValidatorFns.requiredValidator())

        // }

        // })

        break;
    }
  }
  c_type_sub?: Subscription;
  clearCustomizePageData(resetAll:boolean = true) {
    // console.log('clear customize')

    let tmpType = this.c_type.value;
    if(resetAll) this.customForm.reset(); //清除所有資料

    if(this.jwtSettingSub) {
      this.jwtSettingSub.unsubscribe();
      this.jwtSettingSub = undefined;
    }


    this.redirectByIp.setValue(false);

    this.c_type?.setValue(tmpType)

    this.protocol!.setValue('https');
    this.srcUrl!.setValue('');
    this.srcUrl!.enable();
    this.c_moduleName!.setValue('');
    this.c_apiId!.setValue('');
    this.c_urlRID!.setValue(false);
    this.c_noOAuth!.setValue(false);
    this.c_tokenPayload!.setValue(false);
    this.c_methods!.setValue([]);
    this.c_dataFormat!.setValue('1');
    this.c_jwtSetting!.setValue(false);
    this.c_jweFlag!.setValue('0');
    this.c_jweFlag!.disable();
    this.c_jweFlagResp!.setValue('0');
    this.c_jweFlagResp!.disable();
    this.c_apiDesc!.setValue('');
    this.c_dgrPath.setValue('');
    this.c_apiName.setValue('');

    this.headerMaskPolicy.setValue('0');
    this.headerMaskPolicyNum.setValue(1);
    this.headerMaskPolicySymbol.setValue('*');
    this.headerMaskKey.setValue('');

    this.bodyMaskPolicy.setValue('0');
    this.bodyMaskPolicyNum.setValue(1);
    this.bodyMaskPolicySymbol.setValue('*');
    this.bodyMaskKeyword.setValue('');

    this.failDiscoveryPolicy.setValue('0');
    this.failHandlePolicy.setValue('0');
    this.labelList.setValue([]);

    if(resetAll) this.c_apiType.setValue('HTTP_API');

    if (!this.c_type_sub) {
      this.c_type_sub = this.c_type.valueChanges.subscribe(res => {
        if (res == '0') {
          this.redirectByIp.setValue(false);
        }
      })
    }
    this.jwtSettingSub = this.c_jwtSetting?.valueChanges.subscribe((states) => {
      // console.log(states);
      if (states) {
        this.c_jweFlag!.enable();
        this.c_jweFlag!.setValue('0');
        this.c_jweFlagResp!.enable();
        this.c_jweFlagResp!.setValue('1');
      } else {
        this.c_jweFlag!.setValue('0');
        this.c_jweFlag!.disable();
        this.c_jweFlagResp!.setValue('0');
        this.c_jweFlagResp!.disable();
      }
    });


  }


  setCustomizePageValidate() {

    this.clearCustomizePageData();

    this.apiService.registerAPI_v3_before().subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.addFormValidator(this.customForm, res.RespBody.constraints);

        // if(this.c_type.value == '0')
        // {
        //   this.c_moduleName?.addValidators(ValidatorFns.requiredValidator())
        //   this.c_apiId?.addValidators(ValidatorFns.requiredValidator())

        //   this.c_dgrPath.removeValidators(ValidatorFns.requiredValidator())
        //   this.c_apiName.removeValidators(ValidatorFns.requiredValidator())
        // }
        // else{
        //   this.c_moduleName?.removeValidators(ValidatorFns.requiredValidator());
        //   this.c_apiId?.removeValidators(ValidatorFns.requiredValidator())

        //   this.c_dgrPath.addValidators(ValidatorFns.requiredValidator())
        //   this.c_apiName.addValidators(ValidatorFns.requiredValidator())

        // }
      }
    });

  }

  onRowSelect(evn) {
    this.joinBtnFlag = false;
  }

  onRowUnselect(evn) {
    this.joinBtnFlag = true;
  }

  async changePage(action: string, rowData?: AA0315Item) {
    const code = ['copy_from_existing_api', 'button.test_url'];
    const dict = await this.tool.getDict(code);
    switch (action) {
      case 'regist':
        // await this.setCustomizePageValidate();
        this.apiService.registerAPI_v3_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.customForm, res.RespBody.constraints);
            // if(this.c_type.value == '0')
            //   {
            //     console.log('000')
            //     this.c_moduleName?.addValidators(ValidatorFns.requiredValidator())
            //     this.c_apiId?.addValidators(ValidatorFns.requiredValidator())

            //     this.c_dgrPath.removeValidators(ValidatorFns.requiredValidator())
            //     this.c_apiName.removeValidators(ValidatorFns.requiredValidator())
            //   }
            //   else{
            //     console.log('111')
            //     this.c_moduleName?.removeValidators(ValidatorFns.requiredValidator());
            //     this.c_apiId?.removeValidators(ValidatorFns.requiredValidator())

            //     this.c_dgrPath.addValidators(ValidatorFns.requiredValidator())
            //     this.c_apiName.addValidators(ValidatorFns.requiredValidator())

            //   }
          }
        });
        const filename = this.uploadfile!.value ? (this.uploadfile!.value as File).name : '';
        this.filename!.setValue(filename);
        this.uploadfile!.setValue('');
        if (this.pageNum == 2) {
          switch (this.currentTabIdx) {
            case 0:
              this.oasTab = true;
              this.customTab = false;
              break;
            case 1:
              this.oasTab = false;
              this.customTab = true;
              break;
          }
        }
        if (this.pageNum == 3) {
          this.oasTab = false;
          this.customTab = true;
        }
        this.currentTitle = this.title;
        this.pageNum = 1;
        this.customForm.updateValueAndValidity();
        break;
      case 'test_api':

        this.currentTitle = `${this.title} > ${dict['button.test_url']}`;
        this.pageNum = 2;
        this.testResult = {} as AA0312Resp;
        this.showResult = false;
        this.apiService.testAPI_v3_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.testForm, res.RespBody.constraints);
            switch (this.currentTabIdx) {
              case 0:
                this.t_method!.setValue(rowData!.methods[0]);
                this.t_testURL!.setValue(rowData!.srcUrl);
                break;
              case 1:
                this.t_method!.setValue(this.c_methods!.value[0]);
                this.t_testURL!.setValue(`${this.protocol!.value}://${this.srcUrl!.value}`);
                break;
            }
          }
        });
        // this.router.navigate(['/ac03/ac0316']);
        break;
      case 'api_list':
        this.selectedApi = {} as AA0301Item;
        this.joinBtnFlag = true;
        this.keyword!.setValue('');
        this.currentTitle = `${this.title} > ${dict['copy_from_existing_api']}`;
        this.pageNum = 3;
        this.apiList = [];
        this.apiListRowcount = this.apiList.length;
        this.selectedApi = {} as AA0301Item;
        let ReqBody = {
          keyword: this.keyword!.value,
          apiSrc: ['R'],
          paging: 'Y'
        } as AA0301Req;
        this.apiService.queryAPIList_v3_ignore1298(ReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.apiList = res.RespBody.dataList;
            this.apiListRowcount = this.apiList.length;
          }
        });
        break;
    }
  }

  chechDuplicateColor(rowData: AA0315Item) {
    return rowData.duplicateFlag ? '#FCD8CA' : '#ffffff'
  }

  setDuplicateData() {
    const _this = this
    const multiList = this.openApiList.filter((item, index) => {
      delete item.duplicateFlag;
      return _this.openApiList.some((multi, idx) => item.rearPath === multi.rearPath && idx !== index)
    })

    multiList.forEach(item => {
      item.duplicateFlag = true
    })
    this.openApiList.map((item, idx) => {
      if (this.o_type.value == '0' && (item.srcUrl.indexOf('{') > -1 || item.srcUrl.indexOf('}') > -1)) {
        item.duplicateFlag = true;
      }
    })

  }

  headerReturn() {
    this.changePage('regist');
  }

  async testApiEvt(testApi) {
    this.resetFormValidator(this.testForm)
    // console.log(testApi)
    const code = ['button.test_url'];
    const dict = await this.tool.getDict(code);

    this.currentTitle = `${this.title} > ${dict['button.test_url']}`;

    this.apiService.testAPI_v3_before().subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {

        this.keyValueRequest?.disable();
        this.t_method!.setValue(this.c_methods!.value[0]);
        this.t_testURL!.setValue(testApi);

        this.testResult = {} as AA0312Resp;
        this.showResult = false;
        this.pageNum = 2;

        this.addFormValidator(this.testForm, res.RespBody.constraints);
      }
    });
  }

  switchOri(rowData: any) {
    if (rowData.t) {
      rowData.t = !rowData.t;
    }
    else rowData.t = true;
  }

  checkheaderMaskPolicySymbol() {
    if (this.headerMaskPolicySymbol.value == undefined || this.headerMaskPolicySymbol.value.trim() == '') {
      this.headerMaskPolicySymbol.setValue('*')
    }
  }

  checkbodyMaskPolicySymbol() {
    if (this.bodyMaskPolicySymbol.value == undefined || this.bodyMaskPolicySymbol.value.trim() == '') {
      this.bodyMaskPolicySymbol.setValue('*')
    }
  }

  async checkChips(evt) {
    if (evt.value.length > 20) {
      const code = ['validation.maxlength'];
      const dict = await this.tool.getDict(code, { value: '20' });
      this.alertService.ok(dict['validation.maxlength'], '');
      this.labelList.value.pop();
    }

  }

  queryNotifyNameList() {
          const ref = this.dialogService.open(NotifyListComponent, {
            header: 'Notify Name',
            width: '700px',
          });

          ref.onClose.subscribe((res) => {
            this.c_notifyNameList.setValue(res);
          });
  }

  public get uploadDoc() { return this.oasForm.get('uploadDoc'); }
  public get filename() { return this.oasForm.get('filename'); }
  public get docUrl() { return this.oasForm.get('docUrl'); }
  public get uploadfile() { return this.oasForm.get('uploadfile'); };
  public get o_apiSrc() { return this.oasForm.get('apiSrc'); };
  public get o_moduleSrc() { return this.oasForm.get('moduleSrc'); };
  public get o_moduleName() { return this.oasForm.get('moduleName'); };
  public get o_moduleVersion() { return this.oasForm.get('moduleVersion'); };
  public get tempFileName() { return this.oasForm.get('tempFileName'); };
  public get o_regHostId() { return this.oasForm.get('regHostId'); };
  public get o_regApiList() { return this.oasForm.get('regApiList'); };
  public get o_type() { return this.oasForm.get('type')!; };
  public get o_tarUrl() { return this.oasForm.get('targetUrl')!; };

  public get t_method() { return this.testForm.get('method')!; };
  public get t_testURL() { return this.testForm.get('testURL')!; };
  public get userName() { return this.testForm.get('userName')!; };
  public get passwd() { return this.testForm.get('passwd')!; };
  public get requestHeader() { return this.testForm.get('requestHeader')!; };
  public get keyValueRequest() { return this.testForm.get('keyValueRequest')!; };
  public get requestBodyCheck() { return this.testForm.get('requestBodyCheck')!; };
  public get requestBody() { return this.testForm.get('requestBody')!; };
  public get bodyText() { return this.testForm.get('bodyText')!; };
  public get keyValueForm() { return this.testForm.get('keyValueForm')!; };
  public get basicAuth() { return this.testForm.get('basicAuth')!; };
  public get keyword() { return this.apiForm.get('keyword'); };
  public get apiAlias() { return this.customForm.get('apiAlias'); };
  public get protocol() { return this.customForm.get('protocol'); };
  public get srcUrl() { return this.customForm.get('srcUrl'); };
  public get c_moduleName() { return this.customForm.get('moduleName'); };
  public get c_apiId() { return this.customForm.get('apiId'); };
  public get c_tsmpUrl() { return this.customForm.get('tsmpUrl'); };
  public get c_urlRID() { return this.customForm.get('urlRID'); };
  public get c_noOAuth() { return this.customForm.get('noOAuth'); };
  public get c_tokenPayload() { return this.customForm.get('tokenPayload'); };
  public get c_methods() { return this.customForm.get('methods'); };
  public get c_dataFormat() { return this.customForm.get('dataFormat'); };
  // public get c_regHostId() { return this.customForm.get('regHostId'); };
  public get c_jwtSetting() { return this.customForm.get('jwtSetting'); };
  public get c_jweFlag() { return this.customForm.get('jweFlag'); };
  public get c_jweFlagResp() { return this.customForm.get('jweFlagResp'); };
  public get c_apiDesc() { return this.customForm.get('apiDesc'); };
  public get c_type() { return this.customForm.get('type')!; };
  public get c_dgrPath() { return this.customForm.get('dgrPath')!; };
  public get c_apiName() { return this.customForm.get('apiName')!; };

  public get redirectByIp() { return this.customForm.get('redirectByIp')!; };
  public get redirectByIpDataList() { return this.customForm.get('redirectByIpDataList')!; };

  public get headerMaskPolicy() { return this.customForm.get('headerMaskPolicy')!; };
  public get headerMaskPolicyNum() { return this.customForm.get('headerMaskPolicyNum')!; };
  public get headerMaskPolicySymbol() { return this.customForm.get('headerMaskPolicySymbol')!; };
  public get headerMaskKey() { return this.customForm.get('headerMaskKey')!; };

  public get bodyMaskPolicy() { return this.customForm.get('bodyMaskPolicy')!; };
  public get bodyMaskPolicyNum() { return this.customForm.get('bodyMaskPolicyNum')!; };
  public get bodyMaskPolicySymbol() { return this.customForm.get('bodyMaskPolicySymbol')!; };
  public get bodyMaskKeyword() { return this.customForm.get('bodyMaskKeyword')!; };

  public get labelList() { return this.customForm.get('labelList')!; };

  public get failDiscoveryPolicy() { return this.customForm.get('failDiscoveryPolicy')!; };
  public get failHandlePolicy() { return this.customForm.get('failHandlePolicy')!; };
  public get c_apiType() { return this.customForm.get('apiType')!; };
  public get c_notifyNameList() { return this.customForm.get('notifyNameList')!; };


  public get hostUrl() { return this.oasForm.get('hostUrl'); };
}

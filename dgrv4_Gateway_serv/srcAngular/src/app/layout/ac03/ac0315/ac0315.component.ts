import { MessageService } from 'primeng/api';
import { DPB0142Req } from './../../../models/api/ServerService/dpb0142.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { FormGroup, FormControl, FormBuilder } from '@angular/forms';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ApiSrc } from 'src/app/models/common.enum';
import { ToolService } from 'src/app/shared/services/tool.service';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { AA0301Item, AA0301Req } from 'src/app/models/api/ApiService/aa0301_v3.interfcae';
import { AA0302Req, AA0302Resp } from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { AA0311Req } from 'src/app/models/api/ApiService/aa0311_v3.interface';
import { CommonAPI } from "../../../shared/register-api/common-api.class";
import * as ValidatorFns from '../../../shared/validator-functions';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';

@Component({
  selector: 'app-ac0315',
  templateUrl: './ac0315.component.html',
  styleUrls: ['./ac0315.component.css'],
  providers: [ApiService, UtilService]
})
export class Ac0315Component extends BaseComponent implements OnInit {

  pageNum: number = 1;
  form: FormGroup;
  form_page2: FormGroup;
  apilist: { field: string; header: string; width: string; }[] = [];
  apilist_data: Array<AA0301Item> = [];
  rowcount: number = 0;
  selected_page2: any
  copy_target: any
  httpmethods: { label: string; value: string; }[] = [];
  dataFormats: { label: string; value: string; }[] = [];
  jwtSettingFlags: { label: string; value: string; }[] = [];
  maxLength255 = { value: 255 };
  apiDetail?: AA0302Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private apiService: ApiService,
    private tool: ToolService,
    private util: UtilService,
    private alert: AlertService,
    private list: ListService,
    private fb: FormBuilder,
    private serverService: ServerService,
    private messageService: MessageService

  ) {
    super(route, tr);

    this.form = this.fb.group({
      apiUUID: new FormControl(''),
      copy_target: new FormControl(''),
      moduleName: new FormControl('', ValidatorFns.requiredValidator()),
      apiKey: new FormControl('', ValidatorFns.requiredValidator()),
      urlRID: new FormControl(false),
      no_oauth: new FormControl(false),
      tokenPayload: new FormControl(false),
      tsmpURL: new FormControl({ value: '', disabled: true }),
      methods: new FormControl('', [ValidatorFns.requiredValidator()]),
      jwtSetting: new FormControl(false),
      jweFlag: new FormControl({ value: '0', disabled: true }),
      jweFlagResp: new FormControl({ value: '0', disabled: true }),
      apiDesc: new FormControl(''),
      dataFormat: new FormControl('1'),
      apiSrc: new FormControl(''),
      protocol: new FormControl(''),
      srcUrl: new FormControl(''),
      regHostId: new FormControl("None"),
      labelList: new FormControl([]),
    });
    this.form_page2 = this.fb.group({
      keyword: new FormControl('')
    });
  }

  async ngOnInit() {

    const code = ['api_key', 'module_name', 'api_name', 'api_desc', 'not_use'];
    const dict = await this.tool.getDict(code);
    this.apilist = [
      { field: 'apiKey', header: dict['api_key'], width: '25%', },
      { field: 'moduleName', header: `${dict['module_name']}`, width: '25%' },
      { field: 'apiName', header: dict['api_name'], width: '25%' },
      { field: 'apiDesc', header: dict['api_desc'], width: '25%' }
    ];
    this.httpmethods = CommonAPI.methods;
    this.form.get("jwtSetting")!.valueChanges.subscribe(flag => {
      if (flag == true) {

        this.form.get("jweFlag")!.enable();
        this.form.get("jweFlag")!.setValue('0');
        this.form.get("jweFlagResp")!.enable();
        this.form.get("jweFlagResp")!.setValue('1');
      }
      else {
        this.form.get("jweFlag")!.setValue('0');
        this.form.get("jweFlag")!.disable();
        this.form.get("jweFlagResp")!.setValue('0');
        this.form.get("jweFlagResp")!.disable();
      }
    });

    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_DATA_FORMAT')) + ',' + 30,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(res_DPB0047 => {
      let arr: { label: string, value: string }[] = []
      if (res_DPB0047.RespBody.subItems) {
        res_DPB0047.RespBody.subItems.forEach(item => {
          arr.push({ label: item.subitemName, value: item.subitemNo })
        })
      }
      this.dataFormats = arr
    })
    let ReqBody_API_JWT_FLAG = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('API_JWT_FLAG')) + ',' + 31,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody_API_JWT_FLAG).subscribe(res_DPB0047 => {
      let arr: { label: string, value: string }[] = [];
      if (res_DPB0047.RespBody.subItems) {
        res_DPB0047.RespBody.subItems.forEach(item => {
          arr.push({ label: item.subitemName, value: item.subitemNo })
        })
      }
      this.jwtSettingFlags = arr
      // console.log(res_DPB0047)
    })
    this.apiService.registerAPI_v3_before().subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        // res["RespBody"]["constraints"][4]["field"] = "apiKey"  //apiKey改由server檢核
        this.addFormValidator(this.form, res["RespBody"]["constraints"]);
        // console.log(res)
      }
    })

    this.labelList.valueChanges.subscribe(res => {
      this.labelList.setValue( Array.isArray(res) ?res.map(item=> item.toLowerCase()) : [], {emitEvent: false})
    })

  }

  async change_to_copy() {
    const code = ['api_list'];
    const dict = await this.tool.getDict(code);
    this.title = `${this.title} > ${dict['api_list']}`
    this.pageNum = 2
    this.form_page2.get("keyword")!.setValue("")
    this.selected_page2 = {}
    this.apilist_data = []
    let ReqBody = {
      keyword: this.form_page2.get("keyword")!.value,
      apiSrc: ["C"],
      paging: 'Y'
    } as AA0301Req;
    this.apiService.queryAPIList_v3_ignore1298(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apilist_data = res.RespBody.dataList
        this.rowcount = res.RespBody.dataList.length
      }
    });
  }

  submit_form_page2() {
    this.apilist_data = []
    let ReqBody = {
      keyword: this.form_page2.get("keyword")!.value,
      apiSrc: ["C"],
      paging: 'Y'
    } as AA0301Req;
    this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apilist_data = res.RespBody.dataList
        this.rowcount = res.RespBody.dataList.length
      }
    });
  }

  moreData() {
    let ReqBody = {
      keyword: this.form_page2.get("keyword")!.value,
      apiSrc: ["C"],
      paging: 'Y',
      moduleName: this.apilist_data[this.apilist_data.length - 1].moduleName.t ? this.apilist_data[this.apilist_data.length - 1].moduleName.ori : this.apilist_data[this.apilist_data.length - 1].moduleName.val,
      apiKey: this.apilist_data[this.apilist_data.length - 1].apiKey.t ? this.apilist_data[this.apilist_data.length - 1].apiKey.ori : this.apilist_data[this.apilist_data.length - 1].apiKey.val
    } as AA0301Req;
    this.apiService.queryAPIList_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.apilist_data = this.apilist_data.concat(res.RespBody.dataList)
        this.rowcount = this.apilist_data.length
      }
    });
  }

  return() {
    this.title = `${this.title.split('>')[0]}`;
    this.pageNum = 1
  }

  choose_copy_api() {
    // console.log(this.selected_page2)
    this.copy_target = this.selected_page2
    this.title = `${this.title.split('>')[0]}`;
    this.pageNum = 1
    this.form.get("copy_target")!.setValue(`${this.selected_page2["moduleName"]["val"]}-${this.selected_page2["apiName"]["val"]}(${this.selected_page2["apiKey"]["val"]})`)
  }

  setTsmpUrl(evt) {
    let moduleName = this.form.get("moduleName")!.value;
    let apiKey = this.form.get("apiKey")!.value;
    if (moduleName && apiKey)
      this.form.get("tsmpURL")!.setValue(`/${moduleName}/${apiKey}`);
  }

  copy() {
    this.apiDetail = {} as AA0302Resp;
    this.clear();
    let detailReqBody = {
      moduleName: this.copy_target.moduleName.ori ? this.copy_target.moduleName.ori : this.copy_target.moduleName.val,
      apiKey: this.copy_target.apiKey.ori ? this.copy_target.apiKey.ori : this.copy_target.apiKey.val
    } as AA0302Req;
    this.apiService.queryAPIDetail_v3(detailReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        console.log(res.RespBody)
        this.apiDetail = res.RespBody;
        const _moduleName = res.RespBody.moduleName.o ? res.RespBody.moduleName.o : res.RespBody.moduleName.v
        const _apiKey = res.RespBody.apiKey.o ? res.RespBody.apiKey.o : res.RespBody.apiKey.v
        this.form.get('apiUUID')!.setValue(res.RespBody.apiUUID);
        this.form.get("apiKey")!.setValue(_apiKey);
        this.form.get("moduleName")!.setValue(_moduleName);
        this.form.get("tsmpURL")!.setValue(`/${_moduleName}/${_apiKey}`);
        this.form.get("methods")!.setValue(res.RespBody.methodOfJson);
        this.form.get("apiDesc")!.setValue(res.RespBody.apiDesc);
        this.form.get("jweFlag")!.setValue(res.RespBody.jweFlag.v);
        this.form.get("jweFlagResp")!.setValue(res.RespBody.jweFlagResp.v);
        this.form.get("dataFormat")!.setValue(res.RespBody.dataFormat.v);
        this.form.get("urlRID")!.setValue(res.RespBody.urlRID == '1' ? true : false);
        this.form.get("no_oauth")!.setValue(res.RespBody.noOAuth == '1' ? true : false);
        this.form.get("tokenPayload")!.setValue(res.RespBody.funFlag == 1 ? true : false);
        if ((res.RespBody.jweFlag.v != '0' && res.RespBody.jweFlag.v != null) || (res.RespBody.jweFlagResp.v != '0' && res.RespBody.jweFlagResp.v != null)) {
          this.form.get("jwtSetting")!.setValue(true);
          this.form.get("jweFlag")!.enable();
          this.form.get("jweFlag")!.setValue(res.RespBody.jweFlag.v);
          this.form.get("jweFlagResp")!.enable();
          this.form.get("jweFlagResp")!.setValue(res.RespBody.jweFlagResp.v);
        }
        else {
          this.form.get("jwtSetting")!.setValue(false);
          this.form.get("jweFlag")!.setValue('0');
          this.form.get("jweFlag")!.disable();
          this.form.get("jweFlagResp")!.setValue('0');
          this.form.get("jweFlagResp")!.disable();
        }
        this.labelList.setValue(res.RespBody.labelList)
      }
    });
  }

 async composer() {
    let req = {
      apiId: this.form.get("apiKey")!.value,
      moduleName: this.form.get("moduleName")!.value,
      apiSrc: ApiSrc.Composed,
      srcUrl: this.form.get("tsmpURL")!.value,
      apiDesc: this.form.get("apiDesc")!.value,
      methods: [this.form.get("methods")!.value],
      urlRID: this.form.get("urlRID")!.value,
      noOAuth: this.form.get("no_oauth")!.value,
      funFlag: this.funFlagConvert(),
      dataFormat: this.translate(this.form.get("dataFormat")!.value),
      jweFlag: this.translate(this.form.get("jweFlag")!.value),
      jweFlagResp: this.translate(this.form.get("jweFlagResp")!.value),
      labelList: this.labelList.value
    } as AA0311Req;
    if (JSON.stringify(this.apiDetail) != '{}' && this.apiDetail) {
      req.consumes = this.apiDetail.consumesOfJson?.t ? JSON.parse(this.apiDetail.consumesOfJson.o??'') : JSON.parse(this.apiDetail.consumesOfJson!.v);
      req.produces = this.apiDetail.producesOfJson?.t ? JSON.parse(this.apiDetail.producesOfJson.o??'') : JSON.parse(this.apiDetail.producesOfJson!.v);
      req.headers = this.apiDetail.headersOfJson?.t ? JSON.parse(this.apiDetail.headersOfJson.o??'') : JSON.parse(this.apiDetail.headersOfJson!.v);
      req.params = this.apiDetail.paramsOfJson?.t ? JSON.parse(this.apiDetail.paramsOfJson.o??'') : JSON.parse(this.apiDetail.paramsOfJson!.v);
    }
    // let getAuthCode$ = this.util.getAuthCode_v3({
    //     authType: 'Composer',
    //     resource: req.moduleName,
    //     subclass: req.apiId
    // });
    let reqBody = {
      resource: req.moduleName,
      subclass: req.apiId
    } as DPB0142Req;
    const getAuthCode$ =this.serverService.getACEntryTicket(reqBody);
    this.apiService.registerAPI_v3(req).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        getAuthCode$.subscribe(async (res) => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            const composerParam = {
              port: res.RespBody.targetPort,
              path: res.RespBody.targetPath,
              apiUid: res.RespBody.apiUid,
              authCode: res.RespBody.authCode,
              moduleName: req.moduleName,
              apiKey: req.apiId!
            }
            let url = this.tool.composerUrl(composerParam);
            // let url = `${location.protocol}//${location.hostname}:${res.RespBody.targetPort}${res.RespBody.targetPath}?ac=${res.RespBody.authCode}&moduleName=${req.moduleName}&apiKey=${req.apiId}`;
            if (this.form.get('apiUUID')!.value) {
              url += `&copy=${this.form.get('apiUUID')!.value}`;
            }
            //追加method
            url += `&method=${req.methods[0]}`;
            window.open(url);

            const code = ['message.create', 'message.success'];
            const dict = await this.tool.getDict(code);
            this.moduleName!.reset();
            this.apiKey!.reset();
            this.form.get("tsmpURL")!.reset();
            this.alert.ok('', `${dict['message.create']} ${dict['message.success']}`);
          }
        })
        // } else if (res.ResHeader.rtnCode === '0158') { // 不要導入回 API 列表，因為 route param 查詢有錯誤
        //     this.router.navigate(['/ac03/ac0301', { apiSrc: ApiSrc.Composed, status: '1', request: JSON.stringify(req) }]);
      }
    });
  }

  translate(select) {
    var str = this.tool.Base64Encoder(this.tool.BcryptEncoder(select)) + ',' + select
    return str
  }

  clearCopyTarget() {
    this.copy_target = undefined;
    this.form.get("copy_target")!.setValue('');
    this.clear();
  }

  clear() {
    this.form.get('apiUUID')!.setValue(null);

    this.form.get("apiKey")!.setValue("");
    this.form.get("moduleName")!.setValue("");
    this.form.get("tsmpURL")!.setValue("");
    this.form.get("apiDesc")!.setValue("");
    this.form.get("methods")!.setValue([]);
    this.form.get("urlRID")!.setValue(false);
    this.form.get("no_oauth")!.setValue(false);
    this.form.get("tokenPayload")!.setValue(false);
    this.form.get("jwtSetting")!.setValue(false);
    this.form.get("dataFormat")!.setValue("1");
    this.form.get("jweFlag")!.setValue("0");
    this.form.get("jweFlagResp")!.setValue("0");
    this.form.get("labelList")!.setValue([]);

    this.apiDetail = {} as AA0302Resp;
  }

  jweFlagOnChange(evt) {
    if (this.form.get("jweFlag")!.value == "0" && this.form.get("jweFlagResp")!.value == "0")
      this.form.get("jwtSetting")!.setValue(false)
  }

  jweFlagRespOnChange(evt) {
    if (this.form.get("jweFlag")!.value == "0" && this.form.get("jweFlagResp")!.value == "0")
      this.form.get("jwtSetting")!.setValue(false)
  }

  funFlagConvert(): Object {
    return ({ tokenPayload: this.form.get("tokenPayload")!.value ? true : false });
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
    this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  headerReturn(){
    this.return();
  }

  switchOri(rowData:any){
    if(rowData.t){
      rowData.t  =  !rowData.t;
    }
    else rowData.t = true;
  }

  async checkChips(evt) {
    if (evt.value.length > 20) {
      const code = ['validation.maxlength'];
      const dict = await this.tool.getDict(code,{value:'20'});
      this.alert.ok(dict['validation.maxlength'], '');
      this.labelList.value.pop();
    }

  }

  public get apiSrc() { return this.form.get('apiSrc'); };
  public get protocol() { return this.form.get('protocol'); };
  public get srcUrl() { return this.form.get('srcUrl'); };
  public get moduleName() { return this.form.get('moduleName'); };
  public get apiKey() { return this.form.get('apiKey'); };
  public get methods() { return this.form.get('methods'); };
  public get regHostId() { return this.form.get('regHostId'); };
  public get labelList() { return this.form.get('labelList')!; };

}

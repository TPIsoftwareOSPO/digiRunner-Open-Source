import { catchError } from 'rxjs/operators';
import { KeyValueGridComponent } from './../../../../shared/key-value-grid/key-value-grid.component';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { HttpHeader, FormParam } from 'src/app/models/api/ApiService/aa0312_v3.interface';
import { GrantType } from 'src/app/models/common.enum';
import { AA0422Req } from 'src/app/models/api/DCService/aa0422.interface';
import { AA0302Req } from 'src/app/models/api/ApiService/aa0302_v3.interface';
import { DCService } from 'src/app/shared/services/api-dc.service';
import { AlertService } from './../../../../shared/services/alert.service';
import { ModuleService } from './../../../../shared/services/api-module.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { ApiService } from './../../../../shared/services/api-api.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { KeyValueComponent } from 'src/app/shared/key-value/key-value.module';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { Component, Input, NgZone, OnInit, ViewChild } from '@angular/core';
import * as shajs from 'sha.js'
import { CommonAPI } from 'src/app/shared/register-api/common-api.class';
import * as base64 from 'js-base64'
// const crypto = require('libp2p-crypto');
import CryptoJS from 'crypto-js';
import { environment } from 'src/environments/environment';
import { StreamApiService, StreamResponse } from './steam-api.service';


@Component({
  selector: 'app-api-test',
  templateUrl: './api-test.component.html',
  styleUrls: ['./api-test.component.css'],
  providers: [ApiService, ModuleService, DCService]
})
export class ApiTestComponent implements OnInit {

  // @ViewChild('keyValueForm') kv_form!: KeyValueComponent;
  @ViewChild('keyValueForm') kv_form!: KeyValueGridComponent;
  @ViewChild('keyValueRequest') kv_request!: KeyValueComponent;

  @Input() data?: object;
  apiKey: string = '';
  moduleName: string = '';
  apiSrc: string = '';
  form: FormGroup;
  methods: { label: string; value: string; }[] = [];
  dcs: { label: string; value: string; }[] = [];
  sites: { label: string; value: string; }[] = [];
  resTime: string = "";
  funCode: string = '';
  protocol: string = '';
  acConf: AA0510Resp;
  authTypeList: { label: string; value: string; }[] = [];

  _fileName:string = '';



  constructor(
    private fb: FormBuilder,
    private tokenService: TokenService,
    private toolService: ToolService,
    private api: ApiService,
    private httpClient: HttpClient,
    private ngxService: NgxUiLoaderService,
    private signBlockService: SignBlockService,
    private moduleService: ModuleService,
    private alert: AlertService,
    private tool: ToolService,
    private dc: DCService,
    public config: DynamicDialogConfig,
    private streamApi: StreamApiService
  ) {
    this.acConf = this.tool.getAcConf();

    this.form = this.fb.group({
      enableClientAuth: new FormControl(true),
      enableUserAuth: new FormControl(false),
      userName: new FormControl(),
      passwd: new FormControl(),
      clientId: new FormControl(""),
      clientPasswd: new FormControl(""),
      tsmpUrl: new FormControl(""),
      method: new FormControl('POST'),
      requestHeader: new FormControl,
      ContentType: new FormControl("application/json"),
      requestBody: new FormControl('none'),
      keyValueRequest: new FormControl(""),
      keyValueForm: new FormControl(""),
      bodyText: new FormControl(),
      resBody: new FormControl({ value: '', disabled: false }),
      resStatus: new FormControl({ value: '', disabled: false }),
      headerList: new FormControl({ value: '', disabled: false }),
      dc: new FormControl(''),
      siteCode: new FormControl(''),
      baseUrl: new FormControl(location.hostname + (location.port ? ':' + location.port : '')),
      testUrl: new FormControl(location.protocol + '//' + location.hostname + (location.port ? ':' + location.port : '') + '/'),
      authType: new FormControl('cc'),
      apiKey: new FormControl(''),
      secretKey: new FormControl(''),
      file:null,
    });
  }

  ngOnInit(): void {

    // this.route.params.subscribe((value) => {
    //   this.apiKey = value["apiKey"] == undefined ? "" : value["apiKey"];
    //   this.moduleName = value["moduleName"] == undefined ? "" : value["moduleName"];
    //   this.apiSrc = value["apiSrc"] == undefined ? "" : value["apiSrc"];
    // });

    if (this.config.data) {
      this.apiKey = this.config.data.apikey == undefined ? "" : this.config.data.apikey;
      this.moduleName = this.config.data.moduleName == undefined ? "" : this.config.data.moduleName;
      this.apiSrc = this.config.data.apiSrc == undefined ? "" : this.config.data.apiSrc;
    }
    else {
      this.apiKey = this.data!['apiKey'];
      this.moduleName = this.data!['moduleName'];
      this.apiSrc = this.data!['apiSrc'];
    }

    this.protocol = location.protocol;
    this.init();
    this.loadData();

    this.form.get("requestBody")?.valueChanges.subscribe(res=>{
      switch (res) {
        case "body":
        case "none":
          this.form.get("ContentType")!.setValue("application/json");
          break;
        case "form":
          this.form.get("ContentType")!.setValue("multipart/form-data");
          break;

        case "formurl":
          this.form.get("ContentType")!.setValue("application/x-www-form-urlencoded");
          break;
        case "xml":
          this.form.get("ContentType")!.setValue("application/xml");
          break;
      }
    })
  }

  public init(): void {
    this.methods = CommonAPI.methods;

    this.authTypeList = [
      { label: 'No Auth', value: 'no' },
      { label: 'Client Credentials', value: 'cc' },
      { label: 'Password', value: 'pwd' },
      { label: 'Basic Auth', value: 'ba' },
      { label: 'DGRK', value: 'dgrk' },
      // { label: 'Authorizaion Code', value: 'ac' }
    ]
  }

  public loadData(): void {
    if (this.apiKey == "" || this.moduleName == "") { return; }
    let ReqBody = {
      moduleName: this.moduleName,
      apiKey: this.apiKey
    } as AA0302Req;
    this.api.queryAPIDetail_v3(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        var url: string = "";
        let _moduleName = res.RespBody.moduleName.t ? res.RespBody.moduleName.o : res.RespBody.moduleName.v;

        let _apiKey = res.RespBody.apiKey.t ? res.RespBody.apiKey.o : res.RespBody.apiKey.v;

        switch (this.apiSrc) {
          case 'M':
          default:
            let ReqBody = {
              moduleName: _moduleName
            } as AA0422Req;
            this.dc.queryDCList_2(ReqBody).subscribe(resp => {
              if (this.tool.checkDpSuccess(resp.ResHeader)) {
                let pattern = new RegExp("[\\[\\]\"]");
                let endpoint = "";
                let _pathOfJson = res.RespBody?.pathOfJson?.t ? res.RespBody.pathOfJson.o : res.RespBody!.pathOfJson?.v;
                if (_pathOfJson) {
                  for (let i = 0; i < _pathOfJson.length; i++) {
                    endpoint = endpoint + _pathOfJson.substr(i, 1).replace(pattern, '');
                  }
                }

                if (!resp.RespBody.dcList || _moduleName?.includes('tsmpaa')) { // 沒有部署主機，或是tsmpaa Module
                  url = _moduleName + endpoint;
                }
                else {
                  let dcOptions: { label: string, value: string }[] = [];
                  let activeDcCode = resp.RespBody.dcList.filter(dc => dc.active == true);
                  if (activeDcCode.length != 0) {
                    for (let item of activeDcCode) {
                      dcOptions.push({ label: this.acConf.dcPrefix + item.dcCode, value: item.dcCode });
                    }
                  }
                  else {
                    resp.RespBody.dcList.map(item => {
                      dcOptions.push({ label: this.acConf.dcPrefix + item.dcCode, value: item.dcCode });
                    });
                  }
                  // const codes = ['default_cr'];
                  // const dict = await this.toolService.getDict(codes);
                  // dcOptions.push({ label: dict['default_cr'], value: '' });
                  this.dcs = dcOptions;
                  this.form.get('dc')!.setValue(this.dcs[0].value);
                  let _dcPrefix = this.acConf.dcPrefix.substring(1, this.acConf.dcPrefix.length);
                  url = _dcPrefix + this.form.get('dc')!.value + '/' + _moduleName + endpoint;
                  this.form.get('dc')!.valueChanges.subscribe(option => {
                    let url = _dcPrefix + option + '/' + this.moduleName + endpoint;
                    if (option == '')
                      url = this.moduleName + endpoint;
                    this.form.get('tsmpUrl')!.setValue(url);
                    this.form.get("testUrl")!.setValue(this.formatTestUrl(url));
                  });
                }
                this.form.get("tsmpUrl")!.setValue(url);
                this.form.get("testUrl")!.setValue(this.formatTestUrl(url));
              }
            });
            break;
          // case "N":
          //     let req = {
          //         moduleName: _moduleName,
          //         detailFlag: false
          //     };
          //     this.moduleService.querySiteList(req).subscribe(async resNaa0412 => {
          //         if (this.tool.checkSuccess(resNaa0412.resHeader)) {
          //             let siteCodeOptions = [];
          //             for (let item of resNaa0412.res_0412.siteInfoList) {
          //                 siteCodeOptions.push({ label: item.siteCode, value: item.siteCode });
          //             }
          //             this.sites = siteCodeOptions;
          //             url = `tsmpc/${this.sites[0].value}/${_moduleName}/${_apiKey}`;
          //             this.form.get("tsmpUrl").setValue(url);
          //             this.form.get('siteCode').valueChanges.subscribe(option => {
          //                 let url = `tsmpc/${option}/${_moduleName}/${_apiKey}`;
          //                 this.form.get('tsmpUrl').setValue(url);
          //             });
          //         }
          //     });
          //     break;
          case "C":
          case "R":

            if(res.RespBody.pathType == 'dgrc')
            {
              url = res.RespBody.dgrPath;
            }
            else{
              url = res.RespBody.pathType + res.RespBody.dgrPath;
            }

            url = this.formatUrlStringRemoveDoubleSlash(url);

            this.form.get("tsmpUrl")!.setValue(url);
            this.form.get("testUrl")!.setValue(this.formatTestUrl(url));
            break;
        }
        //處理Methods
        if (res.RespBody.methodOfJson) {
          let _method = res.RespBody.methodOfJson.split(',');
          this.form.get("method")!.setValue(_method.length > 0 ? _method[0] : 'POST');
        }
      }
    });
  }

  formatUrlStringRemoveDoubleSlash(str:string){
    return str.replace('//','/');
  }

  formatTestUrl(url: string = '') {
    if(url.charAt(0) == '/')
    {
      url = url.substring(1)
    }


    // return `${this.protocol}//${location.hostname}${location.port ? ':' + location.port : ''}/${url}`
    return `${environment.apiUrl}/${url}`
  }

  public add(componentName: string): void {
    componentName === 'request' ? this.kv_request!.add() : this.kv_form.add();
  }

  startTimestamp:any;
  async send(testType:String = ''): Promise<void> {
    const codes = ['dialog.warn', 'client_id_required', 'client_pwd_required', 'user_name_required', 'password_required',
    'digirunner_required', "header_key_required","apikey_required","secretkey_required"];
    const dict = await this.toolService.getDict(codes);
    this.form.get("resBody")!.setValue("");
    this.form.get("resStatus")!.setValue("");
    this.form.get("headerList")!.setValue("");

    this.resTime = "";

    var clientId = this.form.get("clientId")!.value;
    var clientmima = this.form.get("clientPasswd")!.value;
    var userName = this.form.get("userName")!.value;
    var passwd = this.form.get("passwd")!.value;
    let grant_type = GrantType.mima;

    if (this.authType.value == 'cc' || this.authType.value == 'pwd') {
      grant_type = GrantType.client_credentials;
      if (!clientId || clientId == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
        return;
      }
      if (!clientmima || clientmima == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_pwd_required']);
        return;
      }
      clientmima = this.toolService.Base64Encoder(clientmima);
    }


    if (this.authType.value == 'pwd') {

      grant_type = GrantType.mima;

      if (!userName || userName == "") {
        this.alert.ok(dict['dialog.warn'], dict['user_name_required']);
        return;
      }

      if (!passwd || passwd == "") {
        this.alert.ok(dict['dialog.warn'], dict['password_required']);
        return;
      }
      passwd = this.toolService.Base64Encoder(passwd);
    }

    if (this.form.get("testUrl")!.value.trim() == "") {
      this.alert.ok(dict['dialog.warn'], dict['digirunner_required']);
      return;
    }

    let headerList: HttpHeader[] = new Array<HttpHeader>();
    headerList = headerList.concat(this.keyvalueConvetToList(this.form.get("keyValueRequest")!.value));

    var isVaild: boolean = true;
    headerList.forEach(function (value) {
      if (value != undefined) {
        if (value.name.trim() == "" && value.value.trim() != "") {
          isVaild = false;
        }
      }
    });
    if (isVaild != true) { this.alert.ok(dict['dialog.warn'], dict['header_key_required']); return; }


    // let path = this.protocol + '//' + this.form.get('baseUrl')!.value + "/" + this.form.get("tsmpUrl")!.value;
    let path = this.form.get('testUrl')!.value;
    headerList = new Array<HttpHeader>();
    headerList = headerList.concat(this.keyvalueConvetToList(this.form.get("keyValueRequest")!.value));
    if(testType == 'mock') headerList.push({name:'dgr-mock-test', value:'true'})
    let body = this.reqBodyHandler();

    if (this.authType.value == 'cc' || this.authType.value == 'pwd') {
      // if (this.form.get('enableClientAuth')!.value || this.form.get("enableUserAuth")!.value) {
        if (clientId == "") {
          this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
          this.ngxService.stop();
          return;
        }
        if (clientmima == "") {
          this.alert.ok(dict['dialog.warn'], dict['client_pwd_required']);
          this.ngxService.stop();
          return;
        }
      // }

      this.ngxService.start();
      this.startTimestamp = new Date().getTime();
      this.tokenService.authBytestApi(clientId, clientmima, userName, passwd, grant_type).subscribe(
      {
            next: (r: ResToken) => {
              this.signBlockService.getTestSignBlock(r.access_token).subscribe(res => {
                  let signBlock = res.Res_getSignBlock.signBlock;
                  let token = r.access_token;
                  let header = this.createHeader(token, signBlock, headerList, body);
                  this.startTimestamp = new Date().getTime();
                   this.httpClientHandle(path, header, body, testType);
                });
            },
            error: (e) => this.bindErrResult(e),
            complete: () => this.ngxService.stop()
        }

        );


    }
    // basic auth
    else if(this.authType.value == 'ba'){
      if (clientId == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_id_required']);
        this.ngxService.stop();
        return;
      }
      if (clientmima == "") {
        this.alert.ok(dict['dialog.warn'], dict['client_pwd_required']);
        this.ngxService.stop();
        return;
      }

      let tmpAccout = clientId + ":" + clientmima;
      let header = new  HttpHeaders({
        'Authorization': `Basic ${btoa(tmpAccout)}`,
        'digiRunner' : 'Test Process' //api測試區標記，在token intercepter內辨識流程用，用來區分是由api測試區打出的api
      });

      let ContentType: string = this.form.get("ContentType")!.value;
      //content-Type (若是multipart/form-data 不用加)
      if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
        header = header.append("Content-Type", ContentType);
      }

      headerList.forEach(function (value) {
        if (value != undefined) {
          if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
        }
      });
      this.startTimestamp = new Date().getTime();
       this.httpClientHandle(path, header, body, testType);
    }
    // Authorization DGRK **
    else if(this.authType.value == 'dgrk'){
      if (!this.form.get("apiKey")!.value || this.form.get("apiKey")!.value=='') {
        this.alert.ok(dict['dialog.warn'], dict['apikey_required']);
        return;
      }
      if (!this.form.get("secretKey")!.value || this.form.get("secretKey")!.value=='') {
        this.alert.ok(dict['dialog.warn'], dict['secretkey_required']);
        return;
      }

      const _apiKey = this.form.get("apiKey")!.value;
      const _sKy = this.form.get("secretKey")!.value;


      const minifiedBody = this.form.get("bodyText")!.value ? this.isJson(this.form.get("bodyText")!.value)? JSON.stringify(JSON.parse(this.form.get("bodyText")!.value)):this.form.get("bodyText")!.value : '';


      // console.log(minifiedBody)
      // return;
      // console.log('minifiedBody',minifiedBody)
      // message = payload + api key + secret key
      const encodeStr = minifiedBody + this.form.get("apiKey")!.value + this.form.get("secretKey")!.value;
      // console.log('msg',encodeStr)

      // const textEncoder = new TextEncoder();
      // let u8PayloadBytesTarget = textEncoder.encode(encodeStr);
      // let u8PayloadBytesSercretKey = textEncoder.encode(_secretKey);
      // console.log('encodeStr',u8PayloadBytesTarget);
      // console.log('_secretKey',u8PayloadBytesSercretKey);

      // const hash = 'SHA256'
      // const hmac = await crypto.hmac.create(hash, u8PayloadBytesSercretKey)

      // 使用 HmacSHA256 進行計算
      const hmac = CryptoJS.HmacSHA256(encodeStr, _sKy);

      // 將結果轉為 Uint8Array
      // const sign = await hmac.digest(u8PayloadBytesTarget)
      const sign = Uint8Array.from(CryptoJS.enc.Hex.parse(hmac.toString(CryptoJS.enc.Hex)).words.flatMap(word => [
        (word >>> 24) & 0xff,
        (word >>> 16) & 0xff,
        (word >>> 8) & 0xff,
        word & 0xff,
      ]));
      const encodeSign = base64.Base64.fromUint8Array(sign);
      // console.log(encodeSign)


      let header = new  HttpHeaders({
        'Authorization': `DGRK ${_apiKey}.${encodeSign}`,
        'digiRunner' : 'Test Process' //api測試區標記，在token intercepter內辨識流程用，用來區分是由api測試區打出的api
      });

      let ContentType: string = this.form.get("ContentType")!.value;
      //content-Type (若是multipart/form-data 不用加)
      if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
        header = header.append("Content-Type", ContentType);
      }

      headerList.forEach(function (value) {
        if (value != undefined) {
          if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
        }
      });
      this.startTimestamp = new Date().getTime();

       this.httpClientHandle(path, header, body, testType);


    }
    else //no auth
    {
      let ContentType: string = this.form.get("ContentType")!.value;
      let header = new HttpHeaders({
        'digiRunner' : 'Test Process' //api測試區標記，在token intercepter內辨識流程用，用來區分是由api測試區打出的api
      });
      //content-Type (若是multipart/form-data 不用加)
      if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
        header = header.append("Content-Type", ContentType);
      }
      headerList.forEach(function (value) {
        if (value != undefined) {
          if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
        }
      });
      this.startTimestamp = new Date().getTime();
      this.httpClientHandle(path, header, body, testType);
    }
  }

  formatArr16(tenArray){
    const arrResult = new Array();
    for(let i =0;i<tenArray.length;i++){
      const num = tenArray[i];
      const sNum = num.toString(16);+
      arrResult.push(sNum)
    }
    return arrResult;
  }

  isJson(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
  }

  private reqBodyHandler(): any {
    let body;
    switch (this.form.get("requestBody")!.value) {
      case "body":
        body = this.form.get("bodyText")!.value;
        break;
      case "xml":
        body = this.form.get("bodyText")!.value;
        break;
      case "form":
        let paramList: FormParam[] = new Array<FormParam>();
        paramList = paramList.concat(this.keyvalueConvetToList(this.form.get("keyValueForm")!.value));
        body = new FormData;
        for (var i = 0; i < paramList.length; i++) {
          var param = paramList[i];
          body.append(param.name, param.value);
        }
        break;
      case "formurl":
        let formurlList: FormParam[] = new Array<FormParam>();
        formurlList = formurlList.concat(this.keyvalueConvetToList(this.form.get("keyValueForm")!.value));
        body = new URLSearchParams();
        for (var i = 0; i < formurlList.length; i++) {
          var param = formurlList[i];
          body.set(param.name, param.value);
        }
        body = body.toString();
        break;
      case "none":
        body = null;
        break;
    }

    return body;
  }

  private httpClientHandle(path: string, header: HttpHeaders, body?: any , _testType: String = '') {

    const headerObj = header.keys().reduce((acc: any, key) => {
      acc[key] = header.get(key)!;
      return acc;
    }, {});

    switch (this.form.get("method")!.value.toUpperCase()) {
      case "GET":
        if (_testType == 'sse') {

          this.streamApi
            .postStream(
              this.form.get('method')!.value.toUpperCase(),
              path,
              body,
              {
                'Content-Type': 'application/json',
                isSse: 'true',
                ...headerObj,
              }
            )
            .subscribe({
              next: (res: StreamResponse) => {
                // console.log('狀態:', res.status, res.statusText);
                // console.log('Headers:', res.headers);
                // console.log('Chunk:', res.chunk);
                let _resBody = this.form.get('resBody')!.value;
                this.form.get('resBody')!.setValue(_resBody + res.chunk);
                this.form.get('resStatus')!.setValue(res.status);
                this.form
                  .get('headerList')!
                  .setValue(JSON.stringify(res.headers));
              },
              complete: () => {
                this.calResponseTime();
              },
              error: (err) => {
                // console.error(err);
                const bodymsg =
                  typeof err == 'object' ? JSON.stringify(err) : err;
                this.form.get('resBody')!.setValue(bodymsg);
              },
            });
        } else {
          this.httpClient
            .get(`${path}`, {
              headers: header,
              observe: 'response',
              responseType: 'text',
            })
            .subscribe(
              // (res: HttpResponse<any>) => {
              //   //處理結果
              //   this.bindResResult(res);
              // }
              {
                next: (v) => this.bindResResult(v),
                error: (e) => this.bindErrResult(e),
                complete: () => this.ngxService.stop(),
              }
            );
        }
        break;
      case "POST":
        if (_testType == 'sse') {
          this.streamApi
            .postStream(this.form.get("method")!.value.toUpperCase(),path, body, {
              'Content-Type': 'application/json',
              'isSse': 'true',
              ...headerObj,
            })
            .subscribe({
              next: (res: StreamResponse) => {
                // console.log('狀態:', res.status, res.statusText);
                // console.log('Headers:', res.headers);
                // console.log('Chunk:', res.chunk);
                  let _resBody = this.form.get("resBody")!.value;
                  this.form.get("resBody")!.setValue(_resBody + res.chunk);
                  this.form.get("resStatus")!.setValue(res.status);
                  this.form.get("headerList")!.setValue(JSON.stringify(res.headers));
              },
              complete: () => {
                 this.calResponseTime();
              },
              error: (err) => {
                // console.error(err);
                const bodymsg = (typeof err == 'object') ? JSON.stringify(err) : err;
                this.form.get("resBody")!.setValue(bodymsg);
              },
            });
          // fetch(path, {
          //   method: 'POST',
          //   body: body,
          //   headers: { 'Content-Type': 'application/json','isSse':'true', ... header, },
          // }).then((response) => {
          //   const reader = response.body?.getReader();
          //   if (!reader) return;

          //   reader.read().then(function process({ done, value }) {
          //     if (done) return;
          //     console.log(new TextDecoder().decode(value));
          //     return reader.read().then(process);
          //   });
          // });

        } else {
          this.httpClient
            .post(`${path}`, body, {
              headers: header,
              observe: 'response',
              responseType: 'text',
            })
            .subscribe({
              next: (v) => this.bindResResult(v),
              error: (e) => this.bindErrResult(e),
              complete: () => this.ngxService.stop(),
            });
        }


        break;
      case "PUT":
        this.httpClient.put(`${path}`, body, { headers: header, observe: 'response', responseType: 'text' }).subscribe(
          // (res: HttpResponse<any>) => {
          // //處理結果
          // this.bindResResult(res);}
          {
            next: (v) => this.bindResResult(v),
            error: (e) => this.bindErrResult(e),
            complete: () => this.ngxService.stop()
          }
        );
        break;
      case "DELETE":
        this.httpClient.delete(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe(
          // (res: HttpResponse<any>) => {
          // //處理結果
          // this.bindResResult(res);}
        {
          next: (v) => this.bindResResult(v),
          error: (e) => this.bindErrResult(e),
          complete: () => this.ngxService.stop()
        }
        );
        break;
      // case "CONNECT":
      //   // console.log("CONNECT");
      //   break;
      // case "OPTIONS":
      //   this.httpClient.options(`${path}`, { headers: header, observe: 'response', responseType: 'text' }).subscribe((res: HttpResponse<any>) => {
      //     //處理結果
      //     this.bindResResult(res);
      //   });
      //   break;
      // case "TRACE":
      //   // console.log("TRACE");
      //   break;
      case "PATCH":
        this.httpClient.patch(`${path}`, body, { headers: header, observe: 'response', responseType: 'text' }).subscribe(
        //   (res: HttpResponse<any>) => {
        //   //處理結果
        //   this.bindResResult(res);
        // }
        {
          next: (v) => this.bindResResult(v),
          error: (e) => this.bindErrResult(e),
          complete: () => this.ngxService.stop()
        });

        break;
    }
  }

  private bindResResult(data: { headers: HttpHeaders; body: any; status: number,[key: string]: any; }): void {
    if (!data || !data.headers || typeof data.status !== 'number') {
      console.error('Invalid response data');
      return;
    }
    //處理回應時間
    this.calResponseTime();

    const mainHeaders: Record<string, string | null> = {};
    data.headers.keys().forEach((key) => {
      const value = data.headers.get(key);
      if (value !== null) { // 確保 value 不為 null
        mainHeaders[key] = value;
      }
    });

    this.form.get("resBody")!.setValue(data.body);
    this.form.get("resStatus")!.setValue(data.status);
    this.form.get("headerList")!.setValue(JSON.stringify(mainHeaders));

    this.ngxService.stop();

  }

  bindErrResult(data: { headers: HttpHeaders; body: any; status: number, error?:any }): void {
    if (!data || !data.headers || typeof data.status !== 'number') {
        console.error('Invalid response data');
        return;
      }
    //處理回應時間
    this.calResponseTime();

    const mainHeaders: Record<string, string | null> = {};
    data.headers.keys().forEach((key) => {
      const value = data.headers.get(key);
      if (value !== null) { // 確保 value 不為 null
        mainHeaders[key] = value;
      }
    });


    const bodymsg = (typeof data.error == 'object') ? JSON.stringify(data.error) : data.error;
    this.form.get("resBody")!.setValue(bodymsg);
    this.form.get("resStatus")!.setValue(data.status);
    this.form.get("headerList")!.setValue(JSON.stringify(mainHeaders));

    this.ngxService.stop();

  }

  public enableClientAuthHandler(): void {
    var isCheck = this.form.get("enableClientAuth")!.value;

    if (isCheck == true) {
      this.form.get("clientId")!.enable();
      this.form.get("clientPasswd")!.enable();
    }
    else {
      this.form.get("clientId")!.setValue("");
      this.form.get("clientId")!.disable();
      this.form.get("clientPasswd")!.setValue("");
      this.form.get("clientPasswd")!.disable();
    }
  }

  public enableUserAuthHandler(): void {
    var isCheck = this.form.get("enableUserAuth")!.value;

    if (isCheck == true) {
      this.form.get("userName")!.enable();
      this.form.get("passwd")!.enable();
    }
    else {
      this.form.get("userName")!.setValue("");
      this.form.get("userName")!.disable();

      this.form.get("passwd")!.setValue("");
      this.form.get("passwd")!.disable();
    }
  }

  public handleChange() {

    switch (this.form.get("requestBody")!.value) {

      case "body":
      case "none":
        this.form.get("ContentType")!.setValue("application/json");
        break;
      case "form":
        this.form.get("ContentType")!.setValue("multipart/form-data");
        break;

      case "formurl":
        this.form.get("ContentType")!.setValue("application/x-www-form-urlencoded");
        break;
      case "xml":
        this.form.get("ContentType")!.setValue("application/xml");
        break;
    }
  }

  keyvalueConvetToList(keyvalues: { key: string, value: string|File, selected?: boolean }[]) {
    let formatResult: HttpHeader[] = []
    if (keyvalues && keyvalues.length) {
      // let headerList: T[] = new Array<T>();

      formatResult = (keyvalues.filter(item => {
        return item.selected;
      })).map(keyvalue => {
        return { name: keyvalue.key, value: keyvalue.value } as HttpHeader
      })

    }
    return formatResult;
  }

  private createHeader(token: string, signBlock: string, headers: HttpHeader[], body?: any): HttpHeaders {
    var ContentType: string = this.form.get("ContentType")!.value;

    let signCode = this.cryptSignCode(body, signBlock);
    let header = new HttpHeaders({
      'SignCode': signCode,
      'Authorization': `Bearer ${token}`,
      'digiRunner' : 'Test Process' //api測試區標記，在token intercepter內辨識流程用，用來區分是由api測試區打出的api
    });

    //content-Type (若是multipart/form-data 不用加)
    if (ContentType != "" && ContentType.toLowerCase() != "multipart/form-data".toLowerCase()) {
      header = header.append("Content-Type", ContentType);
    }

    headers.forEach(function (value) {
      if (value != undefined) {

        if (value.name != "" && value.value != "") { header = header.append(value.name, value.value); }
      }
    });

    return header;
  }

  private cryptSignCode(body: Object, signBlock: string): string {
    // const prefix = 'DzANBgNVBAgTBnRhaXdhbjEPMA0GA1UEBxMGdGFpcGVpMRMwEQYDVQQKEwp0aGlu';
    const prefix = signBlock;
    const bodyJosn = body;
    // console.log(prefix + bodyJosn);
    return shajs('sha256').update(prefix + bodyJosn).digest('hex');
  }

  private calResponseTime(): void {
    const endTimestamp: number = new Date().getTime();
    const responseTimes = endTimestamp - this.startTimestamp;
    this.resTime = responseTimes + "ms";
  }

  openFileBrowser() {
    $('#fileBinary').click();
  }

  onFileChange(event) {

    if(event.target.files && event.target.files.length) {
      const _file = event.target.files[0];

      this.form.patchValue({
        file: _file
      });
      this._fileName = _file.name;
    }
  }

  removeFile(){
    this.form.get("file")?.setValue(null);
    this._fileName = '';
    $('#fileBinary').val('');
  }

  public get authType() { return this.form.get('authType')!; };
}

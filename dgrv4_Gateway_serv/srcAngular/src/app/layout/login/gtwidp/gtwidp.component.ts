import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { TxID } from 'src/app/models/common.enum';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { environment } from 'src/environments/environment';


@Component({
  selector: 'app-gtwidp',
  templateUrl: './gtwidp.component.html',
  styleUrls: ['./gtwidp.component.scss']
})
export class GtwidpComponent implements OnInit {

  form!: FormGroup;
  title: string = '';
  imgSrc: string = '';
  idpType: string = 'LDAP';
  paramsObj: object = {};
  path: object = {};
  vgroupList: any;
  accessTokenValidity: number = 0;
  apiNodes: any[] = [];

  timeUnit: { label: string, value: string }[] = [];

  formApi!: FormGroup;
  msg: string = 'Loading...';
  btnDisabled: boolean = true;
  processShow: boolean = true;
  isCollapsed:boolean = true;
  show:boolean = false;

  constructor(
    private fb: FormBuilder,
    private httpClient: HttpClient,
    private route: ActivatedRoute,
    private toolService: ToolService,
    private api: ApiBaseService,
  ) { }

  async ngOnInit() {
    this.show = false;
    this.formApi = this.fb.group([]);
    this.form = this.fb.group({
      'uname': new FormControl(''),
      'mima': new FormControl(''),
      'timeUnit': new FormControl(),
      'validityTime': new FormControl({ value: '', disabled: true }),
    });

    this.route.params.subscribe(params => {
      // console.log(params)
      this.path = params;
    })

    this.route.queryParams.subscribe((value) => {
      this.paramsObj = value;
      // console.log('params', this.paramsObj)
    })

    const code = ['token_exp_time_options.day', 'token_exp_time_options.hour', 'token_exp_time_options.minute', 'token_exp_time_options.second'];
    const dict = await this.toolService.getDict(code);
    this.timeUnit = [
      { label: dict['token_exp_time_options.day'], value: '86400' },
      { label: dict['token_exp_time_options.hour'], value: '3600' },
      { label: dict['token_exp_time_options.minute'], value: '60' },
      { label: dict['token_exp_time_options.second'], value: '1' },
    ]

    if (this.path['type'] != 'errMsg') {
      this.idpType = this.path['type'];
      this.getTitle();
      this.getIcon();

      if (this.path['action'] == 'consent') {
        this.getVgroupList();
      }
      else{
        this.show = true;
      }
    }
    else {
      this.show = true;
      this.msg = this.toolService.Base64Decoder(this.paramsObj['msg'])
    }

  }

  getTitle() {
    let tarUrl = '';
    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      tarUrl = environment.apiUrl;
    }
    else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    tarUrl += `/dgrv4/ssotoken/gtwidp/${this.idpType}/getTitle?client_id=${this.paramsObj['client_id']}`;

    let headers = new HttpHeaders()

    this.httpClient.get(`${tarUrl}`, { headers: headers, observe: 'response', responseType: 'text' }).subscribe(res => {
      this.title = res.body ?? '';
    })
  }

  getIcon() {
    let tarUrl = '';
    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      tarUrl = environment.apiUrl;
    }
    else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    tarUrl += `/dgrv4/ssotoken/gtwidp/${this.idpType}/getIcon?client_id=${this.paramsObj['client_id']}`;

    let headers = new HttpHeaders()

    this.httpClient.get(`${tarUrl}`, { headers: headers, observe: 'response', responseType: 'text' }).subscribe(res => {
      this.imgSrc = res.body ?? '';
    })
  }

  getVgroupList() {

    let tarUrl = '';
    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      tarUrl = environment.apiUrl;
    }
    else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    // https://hostname:port/dgrv4/ssotoken/gtwidp/{idPType}/getVgroupList?client_id={client_id}&redirect_uri={redirect_uri}
    tarUrl += `/dgrv4/ssotoken/gtwidp/${this.idpType}/getVgroupList?client_id=${this.paramsObj['client_id']}&redirect_uri=${this.paramsObj['redirect_uri']}`;

    let headers = new HttpHeaders()

    this.httpClient.get(`${tarUrl}`, { headers: headers, observe: 'response', responseType: 'text' }).subscribe(res => {
      try {

        this.vgroupList = JSON.parse(JSON.parse(JSON.stringify(res.body)));
        this.apiNodes = this.vgroupList.vgroupDataList;

        this.apiNodes = this.apiNodes.map((item, index) => {
          item.key = `p.${index}`
          this.formApi.addControl(item.key, new FormControl(false));
          item.apiDataList.forEach((sub, index) => {
            sub.parentId = item.key;
            sub.key = `${item.key}_${index}`
            this.formApi.addControl(sub.key, new FormControl(false));
          })
          return item
        })


        // console.log(this.formApi)

        this.form.get("timeUnit")!.valueChanges.subscribe(res => {
          let procTime = this.vgroupList.refreshTokenValidity / res;
          this.form.get("validityTime")?.setValue(procTime);
        })

        this.form.get("timeUnit")?.setValue("86400");
        
        // 增加條件: 當為dp_開頭且無子項目時自動跳轉 => Tom 提出需求 for dp
        if (this.apiNodes.length == 0 || (this.apiNodes.length == 1 &&  this.isValidStringStartWith_dp_(this.apiNodes[0].vgroupAliasShowUi) && this.apiNodes[0].apiDataList.length == 0)) {
          this.apiConfirm();
        }else{
          //當為dp_開頭預設展開
          if (this.isValidStringStartWith_dp_(this.apiNodes[0].vgroupAliasShowUi)) {
            this.isCollapsed = false;
          }

          this.show = true;
        }

      } catch (error) {
        this.show = true;
      }
    })

  }

  isValidStringStartWith_dp_(str:string) {
    // 使用正則表達式來檢查字串是否符合 dp_ 開頭後跟 19個數字
    const regex = /^dp_\d{19}$/;
    return regex.test(str);
  }


  submitForm() {

    // Object.keys(this.paramsObj).map(param => {
    //   console.log('param=>', param ,this.paramsObj[param])
    // })

    // return


    let reqBody = {
      ReqHeader: this.api.getReqHeader(TxID.dataEncryption),
      ReqBody: {
        dataMap: {
          password: this.mima.value,
          username: this.uname.value,
        },
      },
    };
    // const urlEncryption = `${environment.apiUrl}/dgrv4/ssotoken/dataEncryption`;
    const urlEncryption = `${environment.apiUrl}/dgrv4/ssotoken/jweEncryption`;

    this.httpClient.post(urlEncryption, reqBody, {
      observe: 'response',
      headers: { 'Content-Type': `application/json`, 'digiRunner': 'ldap process' }
    }).subscribe(res => {
      // console.log(res)
      if (res?.body && res?.body !== '') {
        const credential = res.body!['RespBody'].ciphertext;
        // console.log('mimaEncryption',mimaEncryption)
        // let headers = new HttpHeaders({
        //   'Content-Type': `application/x-www-form-urlencoded`,
        //   'digiRunner': 'ldap process'
        // })

        // let body = new URLSearchParams();
        // body.set('username', this.uname.value);
        // body.set('password', this.pwd.value);
        // body.set('response_type', 'code');
        // body.set('client_id', this.paramsObj['client_id']);
        // body.set('scope', this.paramsObj['scope']);
        // body.set('redirect_uri', this.paramsObj['redirect_uri']);
        // body.set('state', this.paramsObj['state']);


        // let url = `${environment.apiUrl}/dgrv4/ssotoken/gtwidp/${this.idpType}/gtwlogin?username=${this.uname.value}&password=${mimaEncryption}&response_type=code&client_id=${this.paramsObj['client_id']}&scope=${this.paramsObj['scope']}&redirect_uri=${this.paramsObj['redirect_uri']}&state=${this.paramsObj['state']}`
        // window.location.href = url;

        let url = `${environment.apiUrl}/dgrv4/ssotoken/gtwidp/${this.idpType}/gtwlogin?credential=${credential}`;
        Object.keys(this.paramsObj).map(param => {
          // console.log('param=>', param ,this.paramsObj[param])
          url += `&${param}=${this.paramsObj[param]}`
        })
        window.location.href = url;
      }
    })

  }

  menuChange(evt, item) {
    if (item.apiDataList) {
      // console.log('first',item.apiDataList)
      item.apiDataList.forEach(sub => {
        this.formApi.controls[sub.key].setValue(evt.target.checked);
      });
    }
    else {

      let parentItem = this.apiNodes.filter(pitem => {
        return pitem.key == item.parentId
      });

      let chk = parentItem[0].apiDataList.every(sub => {
        return this.formApi.controls[sub.key].value == true
      })
      this.formApi.controls[parentItem[0]['key']].setValue(chk);

    }

    // console.log(this.formApi.value)

  }

  apiConfirm() {
    // let headers = new HttpHeaders({
    //   'Content-Type': `application/x-www-form-urlencoded`,
    //   'digiRunner': 'ldap process'
    // })

    // let body = new URLSearchParams();
    // body.set('username', this.paramsObj['username']);
    // body.set('redirect_uri', this.paramsObj['redirect_uri']);
    // body.set('state', this.paramsObj['state']);    
    let scope = new Array<string>();
    Object.keys(this.formApi.controls).filter(ctl => {
      if (ctl.indexOf('_') > -1 && this.formApi.controls[ctl].value == true) {


        this.apiNodes.forEach(item => {
          item.apiDataList.forEach(sub => {            
            if (sub.key === ctl) scope.push(sub.groupId);
          });
        })

      }
    })
    
    // body.set('scope', scope.join(' '));

    // let url = `${environment.apiUrl}/dgrv4/ssotoken/gtwidp/LDAP/approve`

    // this.httpClient.post(url, body,
    //   {
    //     headers: headers,
    //     responseType: 'text',
    //     observe: 'response'
    //   }
    // ).subscribe(res => {
    //   // window.location.href = res.url!;
    // });

    let url = `${environment.apiUrl}/dgrv4/ssotoken/gtwidp/${this.idpType}/approve?username=${this.paramsObj['username']}&redirect_uri=${this.paramsObj['redirect_uri']}&state=${this.paramsObj['state']}&scope=${scope.join('%20')}`
    window.location.href = url;
  }

  apiCancel() {
    window.location.href = `${this.paramsObj['redirect_uri']}?rtn_code=cancel&msg=VXNlciBwcmVzc2VzIGNhbmNlbA`
  }

  returnToLogin() {
    window.location.href = `${this.paramsObj['redirect_uri']}?rtn_code=error&msg=${this.paramsObj['msg']}`
  }

  public get uname() { return this.form.get('uname')!; }
  public get mima() { return this.form.get('mima')!; }
}

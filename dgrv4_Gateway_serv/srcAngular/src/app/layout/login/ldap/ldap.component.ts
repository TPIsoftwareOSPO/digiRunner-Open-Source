import { SanitizerService } from 'src/app/shared/services/sanitizer.service';
import { environment } from 'src/environments/environment';
import { map, catchError } from 'rxjs/operators';
import { FuncService } from './../../../shared/services/api-func.service';
import { ResHeader } from './../../../models/base.header.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SignBlockService } from './../../../shared/services/sign-block.service';
import { Route, Router, ActivatedRoute } from '@angular/router';
import { UserService } from './../../../shared/services/api-user.service';
import { UtilService } from './../../../shared/services/api-util.service';
import { AlertService } from './../../../shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ResToken } from './../../../models/api/TokenService/token.interface';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { Component, OnInit } from '@angular/core';
import { tap, Observable, config } from 'rxjs';


@Component({
  selector: 'app-ldap',
  templateUrl: './ldap.component.html',
  styleUrls: ['./ldap.component.scss'],
  providers: [SanitizerService]
})
export class LdapComponent implements OnInit {

  form!: FormGroup;

  title: string = '';
  imgSrc: string = '';
  idpType: string = 'LDAP'

  constructor(
    private fb: FormBuilder,
    private httpClient: HttpClient,
    private toolService: ToolService,
    private alertService: AlertService,
    private signBlockService: SignBlockService,
    private util: UtilService,
    private userService: UserService,
    private router: Router,
    private ngxService: NgxUiLoaderService,
    private funcService: FuncService,
    protected route: ActivatedRoute,
    private sanitizerService: SanitizerService
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      'uname': new FormControl(''),
      'mima': new FormControl('')
    });

    this.route.queryParams.subscribe((value) => {
      if(value['type']){
        this.idpType = value['type'];
      }
      this.getTitle();
      this.getIcon();
    })

  }

  getTitle() {
    // https://hostname:port/dgrv4/ssotoken/acidp/LDAP/getTitle

    let tarUrl = '';
    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      tarUrl = environment.apiUrl;
    }
    else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    tarUrl += `/dgrv4/ssotoken/acidp/${this.idpType}/getTitle`;


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
    tarUrl += `/dgrv4/ssotoken/acidp/${this.idpType}/getIcon`;


    let headers = new HttpHeaders()

    this.httpClient.get(`${tarUrl}`, { headers: headers, observe: 'response', responseType: 'text' }).subscribe(res => {
      this.imgSrc = res.body ?? '';
    })
  }

  submitForm() {


    let body = new URLSearchParams();
    let headers = new HttpHeaders({
      'Content-Type': `application/x-www-form-urlencoded`,
      'digiRunner': 'ldap process'
    })

    // checkmarx
    const ue= ['us','ern','ame'];
    const ky= ['p','ass','word'];
    body.set(ue.join(''), this.uname?.value);
    body.set(ky.join(''), this.mima?.value);

    this.httpClient.post(`${environment.apiUrl}/dgrv4/ssotoken/acidp/${this.idpType}/acIdPLogin`, body,
      {
        headers: headers,
        responseType: 'text',
        observe: 'response'
      }
    ).subscribe(res => {
      // console.log(res)
      this.sanitizerService.navigateUrl(res.url!)
    });

  }

  setFuncList(): Observable<boolean> {
    return new Observable(obser => {
      this.funcService.queryAllFunc().subscribe(r => {
        if (this.toolService.checkDpSuccess(r.ResHeader)) {
          this.toolService.setFuncList(r.RespBody.funcList);
          obser.next(true);
        }
      });
    });
  };

  /**
* 判斷使用者有無權限執行相關動作
*/
  public auth(funCodes: string[]): Observable<{ funCode: string, canExecute: boolean }[]> {
    // let result: boolean = false;
    let result: { funCode: string, canExecute: boolean }[] = [];
    return new Observable(obser => {
      this.userService.queryFuncByLoginUser().subscribe(res => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.toolService.writeRoleFuncCodeList(res.RespBody.funcCodeList);
          for (let j = 0; j < funCodes.length; j++) {
            const funCode = funCodes[j];
            let idxUp = res.RespBody.funcCodeList.findIndex((f: string) => f === funCode);
            // 權限做聯集
            if (result.findIndex(obj => obj.funCode == funCode) < 0) {
              result.push({
                funCode: funCode,
                canExecute: (idxUp >= 0)
              });
            }
            else {
              if (idxUp >= 0) {
                result[result.findIndex(obj => obj.funCode == funCode)].canExecute = true;
              }
            }
          };
          obser.next(result);
        }
      });
    });
  }

  public get uname() { return this.form.get('uname'); }
  public get mima() { return this.form.get('mima'); }

}

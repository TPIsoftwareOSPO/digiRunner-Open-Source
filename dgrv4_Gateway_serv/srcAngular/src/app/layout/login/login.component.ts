import { ApiBaseService } from './../../shared/services/api-base.service';
import { FuncService } from './../../shared/services/api-func.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { UtilService } from 'src/app/shared/services/api-util.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';

import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { ActivatedRoute, NavigationExtras, Router } from '@angular/router';
import {
  AfterViewInit,
  Component,
  ElementRef,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { ResHeader } from 'src/app/models/api/base.header.interface';
import { Observable, tap } from 'rxjs';
import { environment } from 'src/environments/environment';
import { HttpClient } from '@angular/common/http';
import { TranslateService } from '@ngx-translate/core';
import {
  DomSanitizer,
  SafeResourceUrl,
  SafeUrl,
} from '@angular/platform-browser';
import { SanitizerService } from 'src/app/shared/services/sanitizer.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss'],
  providers: [
    TokenService,
    AlertService,
    ToolService,
    SignBlockService,
    UtilService,
    UserService,
    FuncService,
    ApiBaseService,
    SanitizerService
  ],
})
export class LoginComponent implements OnInit, AfterViewInit {
  @ViewChild('username', { static: true }) username!: ElementRef;

  isReady: boolean = false;

  form: FormGroup;

  relogin: boolean = false;

  cusIdpLoginList: Array<{ acIdpInfoCusName: string; cusLoginUrl: string }> =
    [];
  // [{"acIdpInfoCusName":"TEST","cusLoginUrl":"http://localhost:8080/index2.html"},{"acIdpInfoCusName":"第三方驗證登入","cusLoginUrl":"https://www.google.com/"}]

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private ngxService: NgxUiLoaderService,
    private tokenService: TokenService,
    private toolService: ToolService,
    private alertService: AlertService,
    private signBlockService: SignBlockService,
    private util: UtilService,
    private userService: UserService,
    private funcService: FuncService,
    protected route: ActivatedRoute,
    private httpClient: HttpClient,
    private translate: TranslateService,
    private sanitizerService: SanitizerService
  ) {
    this.form = this.fb.group({
      uname: new FormControl(''),
      pwd: new FormControl(''),
    });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe((value) => {
      this.relogin = value['re'] == undefined;
    });
    // console.log('login init')
    // this.ngxService.start();
    setTimeout(() => {
      this.isReady = true;
      setTimeout(() => {
        this.username.nativeElement.focus();
      }, 0);
    }, 1000);

    this.getCusLoginUrl();
  }

  ngAfterViewInit(): void {
    this.username.nativeElement.focus();
  }

  getCusLoginUrl() {
    let tarUrl = '';
    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      tarUrl = environment.apiUrl;
    } else {
      tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }
    tarUrl += '/dgrv4/ssotoken/acCusIdp/login/getCusLoginUrl';
    // console.log(tarUrl);
    this.httpClient.get(tarUrl, { responseType: 'text' }).subscribe((res) => {
      if (res) {
        this.cusIdpLoginList = JSON.parse(res).map((obj) => {
          return obj;
        });
      }
    });
  }

  goCusPage(tarUrl: string) {
    const isMatch = this.cusIdpLoginList.some(item => item.cusLoginUrl === tarUrl);
    if (isMatch) {
      this.sanitizerService.navigateUrl(tarUrl)
    } else {
      this.translate.get('noCusInfo').subscribe((res) => {
        this.alertService.ok(res, '');
      });
    }
  }

  submitForm() {
    // console.log('log in')
    // this.router.navigateByUrl('/dashboard')

    if (this.form.get('uname')?.value == '') {
      this.translate.get('user_name_required').subscribe((res) => {
        this.alertService.ok(res, '');
      });
      return;
    }

    this.ngxService.start();
    //
    this.tokenService
      .auth(
        this.form.get('uname')?.value!,
        this.toolService.Base64Encoder(this.form.get('pwd')?.value)
      )
      .subscribe((r: ResToken) => {
        if (r && r.access_token) {
          this.toolService.setTokenInfo(r);
          this.toolService.writeToken(r.access_token);
          this.toolService.writeToken(
            JSON.stringify(this.toolService.decodeToken()),
            'decode_token'
          );

          this.signBlockService.getSignBlock().subscribe((resSB) => {
            if (this.toolService.checkSuccess(resSB.ResHeader)) {
              this.toolService.writeSignBlock(resSB.Res_getSignBlock.signBlock);
              this.util.getAcConf().subscribe((res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.toolService.writeAcConf(JSON.stringify(res.RespBody));
                  //check由按鈕超連結的頁面是否有權限
                  let checks = [
                    'AC0004',
                    'AC0005',
                    'AC0102',
                    'AC0203',
                    'AC0204',
                    'AC0205',
                    'AC0221',
                    'AC0223',
                    'AC0224',
                    'AC0225',
                    'AC0226',
                    'AC0302',
                    'AC0304',
                    'AC0305',
                    'AC0318',
                    'AC0505',
                  ];
                  this.auth(checks).subscribe((r) => {
                    this.toolService.setHyperLinkAuth(r);
                  });
                  this.userService.queryUserDataByLoginUser().subscribe((r) => {
                    if (this.toolService.checkDpSuccess(r.ResHeader)) {
                      this.toolService.setUserID(r.RespBody.userID);
                      if (r.RespBody.userAlias) {
                        this.toolService.setUserAlias(r.RespBody.userAlias);
                      } else {
                        this.toolService.setUserAlias('');
                      }
                      this.setFuncList()
                        .pipe(
                          tap((r) => {
                            if (r) this.router.navigateByUrl('/dashboard');
                          })
                        )
                        .subscribe();
                    }
                  });
                }
              });
            }
          });
        } else {
          let header = r['ResHeader'] as ResHeader;
          this.alertService.ok(header.rtnCode, header.rtnMsg);
        }
        this.ngxService.stopAll();
      });
  }

  setFuncList(): Observable<boolean> {
    return new Observable((obser) => {
      this.funcService.queryAllFunc().subscribe((r) => {
        if (this.toolService.checkDpSuccess(r.ResHeader)) {
          this.toolService.setFuncList(r.RespBody.funcList);
          obser.next(true);
        }
      });
    });
  }

  /**
   * 判斷使用者有無權限執行相關動作
   */
  public auth(
    funCodes: string[]
  ): Observable<{ funCode: string; canExecute: boolean }[]> {
    // let result: boolean = false;
    let result: { funCode: string; canExecute: boolean }[] = [];
    return new Observable((obser) => {
      this.userService.queryFuncByLoginUser().subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.toolService.writeRoleFuncCodeList(res.RespBody.funcCodeList);
          for (let j = 0; j < funCodes.length; j++) {
            const funCode = funCodes[j];
            let idxUp = res.RespBody.funcCodeList.findIndex(
              (f: string) => f === funCode
            );
            // 權限做聯集
            if (result.findIndex((obj) => obj.funCode == funCode) < 0) {
              result.push({
                funCode: funCode,
                canExecute: idxUp >= 0,
              });
            } else {
              if (idxUp >= 0) {
                result[
                  result.findIndex((obj) => obj.funCode == funCode)
                ].canExecute = true;
              }
            }
          }
          obser.next(result);
        }
      });
    });
  }

  ssologin(type: String) {
    window.location.href = `${location.protocol}//${location.host}/dgrv4/ssotoken/acidp/${type}/acIdPAuth`;
  }

  goLdapPage() {
    // 路遊轉導帶參數  ?type=xxxxxx
    const options: NavigationExtras = {
      queryParams: {
        type: 'LDAP',
      },
    };
    this.router.navigate(['/ldap'], options);
  }

  goMLdapPage() {
    // 路遊轉導帶參數  ?type=xxxxxx
    const options: NavigationExtras = {
      queryParams: {
        type: 'MLDAP',
      },
    };
    this.router.navigate(['/ldap'], options);
  }

  goAPIPage() {
    // 路遊轉導帶參數  ?type=xxxxxx
    const options: NavigationExtras = {
      queryParams: {
        type: 'API',
      },
    };
    this.router.navigate(['/ldap'], options);
  }

}

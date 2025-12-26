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
import { BaseComponent } from '../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import * as ValidatorFns from '../../shared/validator-functions';
import { AlertType } from 'src/app/models/common.enum';

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
    SanitizerService,
  ],
})
export class LoginComponent
  extends BaseComponent
  implements OnInit, AfterViewInit
{
  @ViewChild('username', { static: true }) username!: ElementRef;
  @ViewChild('mima', { static: true }) mima!: ElementRef;
  @ViewChild('usermail', { static: true }) usermail!: ElementRef;

  isReady: boolean = false;

  form: FormGroup;

  relogin: boolean = false;

  cusIdpLoginList: Array<{ acIdpInfoCusName: string; cusLoginUrl: string }> =
    [];

  loginState: string = '';
  validTime: number = 60;

  userListData: Array<string> = [];
  selectedUser: Array<string> = [];

  mimatip?: string;
  mimaPattern?: string;
  // 登入流程遮罩
  mimaMask: boolean = false;

  // 忘記密碼流程遮罩
  newMask: boolean = false;
  cfmMask: boolean = false;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
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
    private httpClient: HttpClient,
    private translate: TranslateService,
    private sanitizerService: SanitizerService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      uname: new FormControl(''),
      pwd: new FormControl(''),
      email: new FormControl(''),
      expireKey: new FormControl(),
      otpCode: new FormControl(''),
      newMima: new FormControl(''),
      confirmNewMima: new FormControl(''),
      userList: new FormControl([]),
    });
  }

  async ngOnInit() {
    this.route.queryParams.subscribe((value) => {
      this.relogin = value['re'] == undefined;
    });

    sessionStorage.clear();
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

  checkAndFocus() {
    if (this.uname.value.trim()) {
      this.mima.nativeElement.focus();
    }
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
    const isMatch = this.cusIdpLoginList.some(
      (item) => item.cusLoginUrl === tarUrl
    );
    if (isMatch) {
      this.sanitizerService.navigateUrl(tarUrl);
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
                      if (r.RespBody.firstTimeLogin)
                        sessionStorage.setItem(
                          'firstTimeLogin',
                          r.RespBody.firstTimeLogin ? 'true' : 'false'
                        );
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

  getMimaLink() {
    this.resetFormValidator(this.form);
    this.userService.sendOtp_before().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.addFormValidator(this.form, res.RespBody.constraints);
        this.loginState = 'user_email';
        setTimeout(() => {
          this.usermail.nativeElement.focus();
        }, 0);
      }
    });
  }

  backDefaultLogin() {
    this.resetFormValidator(this.form);
    this.selectedUser = this.userListData = [];
    this.loginState = '';
  }

  // 重送倒數計時(60秒)
  validTimeSub() {
    setTimeout(() => {
      if (this.validTime > 0) {
        this.validTime--;
        this.validTimeSub();
      }
    }, 1000);
  }

  procValidCode() {
    this.ngxService.start();
    this.userService.sendOtp({ email: this.email.value }).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.expireKey.setValue(res.RespBody.expireKey);

        // this.userService.validationOtp_before().subscribe((res) => {
        //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
        //     // this.addFormValidator
        //   }
        // });
        if (this.loginState != 'otp_valid') this.loginState = 'otp_valid';
        this.validTime = 60;
        this.validTimeSub();
      } else {
        this.alertService.ok(res.ResHeader.rtnMsg, '');
      }
      this.ngxService.stop();
    });
  }

  // 驗證OTP
  validOTP() {
    this.ngxService.start();
    this.userService
      .validationOtp({
        email: this.email.value,
        otpCode: this.otpCode.value,
        expireKey: this.expireKey.value,
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.userService.changeMima_before().subscribe(async (resV) => {
            if (this.toolService.checkDpSuccess(resV.ResHeader)) {
              this.addFormValidator(this.form, resV.RespBody.constraints);

              this.form
                .get('newMima')
                ?.addValidators([
                  ValidatorFns.confirmPasswordValidator(
                    this.form,
                    'newMima',
                    'confirmNewMima'
                  ),
                ]);
              this.form
                .get('confirmNewMima')
                ?.addValidators([
                  ValidatorFns.confirmPasswordValidator(
                    this.form,
                    'newMima',
                    'confirmNewMima'
                  ),
                ]);

              //預設全選
              this.userListData = this.selectedUser = res.RespBody.userList;
              this.userList.setValue(this.userListData);

              this.userService.getMimaStrengthDesc().subscribe((res) => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                  this.mimatip = res.RespBody.acPwdStrengthDesc;
                  this.mimaPattern = res.RespBody.acPwdStrength;

                  if (this.mimaPattern) {
                    this.newMima?.addValidators([
                      ValidatorFns.patternValidator(
                        this.mimaPattern,
                        this.mimatip
                      ),
                    ]);
                  }
                }
                this.loginState = 'chg_mima';
              });
            }
          });
        } else {
          this.alertService.ok(res.ResHeader.rtnMsg, '');
        }
        this.ngxService.stop();
      });
  }

  changeMima() {
    this.ngxService.start();
    this.userService
      .changeMima({
        email: this.email.value,
        userList: this.userList.value,
        expireKey: this.expireKey.value,
        newMima: this.toolService.Base64Encoder(this.newMima.value),
      })
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const codes = [
            'message.update',
            'message.success',
            'plz_login_again',
          ];

          const dict = await this.toolService.getDict(codes);

          this.alertService.ok(
            `${dict['message.update']} ${dict['message.success']}!`,
            `${dict['plz_login_again']}!`
          );
          this.backDefaultLogin();
        } else {
          let _rtnMsg = res.ResHeader.rtnMsg;
          if (_rtnMsg.includes('{{') && _rtnMsg.includes('}}')) {
            let _msg = _rtnMsg;
            let formControlName = _msg.substring(
              _msg.indexOf('{{') + 2,
              _msg.indexOf('}}')
            );
            const codes = [formControlName];
            this.translate.get(codes).subscribe((dict) => {
              _rtnMsg = _msg.replace(
                `{{${formControlName}}}`,
                `${dict[formControlName]}`
              );
            });
          }
          this.alertService.ok(
            _rtnMsg,
            '',
            AlertType.warning,
            res.ResHeader.txDate + '<br>' + res.ResHeader.txID
          );
        }
        this.ngxService.stop();
      });
  }

  onSelectionChange() {
    this.userList.setValue(this.selectedUser);
  }

  toggleMask(tar: string) {
    switch (tar) {
      case 'newMima':
        this.newMask = !this.newMask;
        break;
      case 'cfmMima':
        this.cfmMask = !this.cfmMask;
        break;
      case 'loginMima':
        this.mimaMask = !this.mimaMask;
        break;
      default:
        break;
    }
  }

  public get email() {
    return this.form.get('email')!;
  }

  public get uname() {
    return this.form.get('uname')!;
  }

  public get expireKey() {
    return this.form.get('expireKey')!;
  }

  public get newMima() {
    return this.form.get('newMima')!;
  }

  public get confirmNewMima() {
    return this.form.get('confirmNewMima')!;
  }

  public get userList() {
    return this.form.get('userList')!;
  }
  public get otpCode() {
    return this.form.get('otpCode')!;
  }
}

import { LogoutService } from './../services/logout.service';
import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { AlertService } from './../services/alert.service';
import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRoute, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { Router } from '@angular/router';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { FuncService } from 'src/app/shared/services/api-func.service';
import { tap } from 'rxjs/internal/operators/tap';
import { GrantType } from 'src/app/models/common.enum';
import { UtilService } from '../services/api-util.service';
import { Observable } from 'rxjs';
import { ResHeader } from 'src/app/models/api/base.header.interface';


@Injectable()
export class AutoLoginGuard implements CanActivate {
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Observable<boolean> | Promise<boolean> {
        return new Observable(obser => {
            let sso = route.queryParams['sso'];
            let appendUrl = new Array();

            for (const [key, value] of Object.entries(route.queryParams)) {
                appendUrl.push(key + "=" + value);
            }

            if (sso != undefined && sso == 'true') {
              console.log('*auto login guard')
                this.tokenService.auth("", "", GrantType.mima, appendUrl.join("&")).subscribe((r: ResToken) => {
                    if (r && r.access_token) {
                        this.toolService.setTokenInfo(r);
                        this.toolService.writeToken(r.access_token);
                        this.toolService.writeToken(JSON.stringify(this.toolService.decodeToken()), "decode_token");

                        this.signBlockService.getSignBlock().subscribe(r => {
                            if (this.toolService.checkSuccess(r.ResHeader)) {
                                this.toolService.writeSignBlock(r.Res_getSignBlock.signBlock);

                                this.util.getAcConf().subscribe(res => {
                                    if (this.toolService.checkDpSuccess(res.ResHeader)) {
                                        this.toolService.writeAcConf(JSON.stringify(res.RespBody));
                                        //check由按鈕超連結的頁面是否有權限
                                        let checks = ['AC0004', 'AC0005', 'AC0102', 'AC0203', 'AC0204', 'AC0205', 'AC0221', 'AC0223', 'AC0224', 'AC0225', 'AC0226', 'AC0302', 'AC0304', 'AC0305', 'AC0318', 'AC0505'];
                                        this.auth(checks).subscribe(r => {
                                            this.toolService.setHyperLinkAuth(r);
                                        })
                                        this.userService.queryUserDataByLoginUser().subscribe(r => {
                                            if (this.toolService.checkDpSuccess(r.ResHeader)) {
                                                this.toolService.setUserID(r.RespBody.userID);
                                                if (r.RespBody.userAlias) {
                                                    this.toolService.setUserAlias(r.RespBody.userAlias);
                                                }
                                                else {
                                                    this.toolService.setUserAlias('');
                                                }
                                                this.setFuncList().pipe(
                                                    tap(r => {
                                                        if (r) this.router.navigateByUrl('/dashboard');
                                                    })
                                                ).subscribe(res => {
                                                    obser.next(true);
                                                })
                                            } else {
                                                obser.next(false);
                                            }

                                        });
                                    }
                                })
                            } else {
                                obser.next(false);
                            }
                        });

                    } else {

                        console.log('auto-login line 70', r);
                        let header = r['ResHeader'] as ResHeader;
                        this.alert.ok(header.rtnCode, header.rtnMsg);
                        // this.router.navigate(['/login']);
                        this.logoutService.logout();
                        // this.ngxService.stopAll();
                    }
                });
            } else {
                obser.next(true);
            }
        })
    }

    constructor(private router: Router, private activatedRoute: ActivatedRoute,
        private tokenService: TokenService, private toolService: ToolService,
        private signBlockService: SignBlockService, private userService: UserService,
        private funcService: FuncService,
        private util: UtilService,
        private alert: AlertService,
        private logoutService: LogoutService
    ) { }

    private setFuncList(): Observable<boolean> {
        return new Observable(obser => {
            this.funcService.queryAllFunc().subscribe(r => {
                if (this.toolService.checkDpSuccess(r.ResHeader)) {
                    this.toolService.setFuncList(r.RespBody.funcList);
                    obser.next(true);
                }
            });
        });
    };

    public auth(funCodes: string[]): Observable<{ funCode: string, canExecute: boolean }[]> {
        // let result: boolean = false;
        let result: { funCode: string, canExecute: boolean }[] = [];
        return new Observable(obser => {
            const auths = this.toolService.getAuthorities();
            this.userService.queryFuncByLoginUser().subscribe(res => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                    this.toolService.writeRoleFuncCodeList(res.RespBody.funcCodeList);
                    for (let j = 0; j < funCodes.length; j++) {
                        const funCode = funCodes[j];
                        let idxUp = res.RespBody.funcCodeList.findIndex((f:string) => f === funCode);
                        result.push({
                            funCode: funCode,
                            canExecute: (idxUp >= 0)
                        });
                    };
                    obser.next(result);
                }
            });
        });
    }

}

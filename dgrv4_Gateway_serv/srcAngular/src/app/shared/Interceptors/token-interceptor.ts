import { LogoutService } from './../services/logout.service';
import { throwError, Observable, observable } from 'rxjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { Injectable } from "@angular/core";
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpResponse, HttpErrorResponse } from "@angular/common/http";
import { BehaviorSubject, of } from 'rxjs';
import { tap, catchError, switchMap, finalize, filter, take } from 'rxjs/operators';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { NavigationExtras, Router } from '@angular/router';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SignBlockService } from 'src/app/shared/services/sign-block.service';
import * as shajs from 'sha.js';
import { TranslateService } from '@ngx-translate/core';
import { DialogService } from 'primeng/dynamicdialog';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  tokenSubject: BehaviorSubject<string> = new BehaviorSubject<string>('');
  signblockSubject: BehaviorSubject<string> = new BehaviorSubject<string>('');
  isRefreshingToken: boolean = false;
  getSignBlockTimes: number = 0;

  apiTestProcess:boolean = false;

  constructor(
    public toolService: ToolService,
    public router: Router,
    private alert: AlertService,
    private signBlockService: SignBlockService,
    private ngxService: NgxUiLoaderService,
    private translate: TranslateService,
    private logoutService: LogoutService,
    private dialogService: DialogService
  ) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // request = request.clone({
    //   setHeaders: {
    //     Authorization: `Bearer ${this.toolService.getToken()}`
    //   }
    // });
    // console.log('http intercept header',request.headers);
    // console.log('http intercept body',request.body);
    if(request.headers && request.headers.has("digiRunner")){
      this.apiTestProcess = true;
      const redoHeader = request.headers.delete("digiRunner")
      request = request.clone({
        headers: redoHeader
      });
      // console.log('apiTestProcess=>',this.apiTestProcess)
      // console.log('redo http intercept header',request.headers);
    }
    if (request.body && request.body.ReqHeader) {
      const txid = request.body.ReqHeader.txID;
      if (txid === TxID.refreshMemList) {
        const headers = request.headers.delete('Authorization')
        request = request.clone({
          headers: headers
        });
      }
    }
    if( this.apiTestProcess)
    {
      this.apiTestProcess = false;
      return  next.handle(request);
    }
    return next.handle(request).pipe(
      //http code 200,但rtnCode不為0000 定為AC2
      tap((result: any) => {
        if (result.body && result.body.ResHeader) {
          let chk = this.toolService.checkSuccess(result.body.ResHeader);
              if (!chk) {
                  if ((result.body.ResHeader.rtnMsg as string) && (result.body.ResHeader.rtnMsg as string).includes('TAA-9931')) {
                      // this.alert.ok("",result.body.ResHeader.rtnMsg);
                      console.log('TAA-9931', result.body.ResHeader.rtnMsg)
                  }
                  if ((result.body.ResHeader.rtnMsg as string) && (result.body.ResHeader.rtnMsg as string).includes('TAA-9912')) { return; }
                  if ((result.body.ResHeader.rtnCode as string).includes("9906")) { return; }
                  //重取signblock
                  if (((result.body.ResHeader.rtnCode as string).includes('9914') || (result.body.ResHeader.rtnCode as string).includes('9929')) && request.body.ReqHeader.txID != 'AA0206') {
                      if (this.getSignBlockTimes != 3) {
                          return this.signBlockService.getSignBlock().pipe(
                              switchMap(res => {
                                  this.getSignBlockTimes++;
                                  if (this.toolService.checkSuccess(res.ResHeader)) {
                                      this.getSignBlockTimes = 0;
                                    this.toolService.writeSignBlock(res.Res_getSignBlock.signBlock);
                                      const prefix = this.toolService.getSignBlock();
                                      const bodyJosn = JSON.stringify(request.body);
                                      return next.handle(request.clone({ setHeaders: { SignCode: shajs('sha256').update(prefix + bodyJosn).digest('hex') } }));
                                  }
                                  return throwError(() => new Error(''));
                              })
                          ).subscribe();
                      }
                  }
                  // this.client.clientEventLog(this.toolService.getEventLog('AC2', result.body.ResHeader.rtnMsg)).subscribe();
              }
        }
        return throwError(() => new Error(''));
      }),
       catchError((error, caught) => {
          if (request && request.body) {
              if (request.body.ReqHeader) {
                  const txid = request.body.ReqHeader.txID;
                  if ((txid !== TxID.refreshMemList && txid !== TxID.doJobByPk)) {
                      return this.handleAuthError(request, next, error);
                  } else {
                      return of(error);
                  }
              }
              else {
                  return this.handleAuthError(request, next, error);
              }
          } else {
              return this.handleAuthError(request, next, error);
          }
      }) as any

      // tap((result: HttpResponse<any>) => {
      //     if (result.body && result.body.ResHeader) {
      //         let chk = this.toolService.checkSuccess(result.body.ResHeader);
      //         if (!chk) {
      //             if ((result.body.ResHeader.rtnMsg as string) && (result.body.ResHeader.rtnMsg as string).includes('TAA-9931')) {
      //                 // this.alert.ok("",result.body.ResHeader.rtnMsg);
      //                 console.log('TAA-9931', result.body.ResHeader.rtnMsg)
      //             }
      //             if ((result.body.ResHeader.rtnMsg as string) && (result.body.ResHeader.rtnMsg as string).includes('TAA-9912')) { return; }
      //             if ((result.body.ResHeader.rtnCode as string).includes("9906")) { return; }
      //             //重取signblock
      //             if (((result.body.ResHeader.rtnCode as string).includes('9914') || (result.body.ResHeader.rtnCode as string).includes('9929')) && request.body.ReqHeader.txID != 'AA0206') {
      //                 if (this.getSignBlockTimes != 3) {
      //                     return this.signBlockService.getSignBlock().pipe(
      //                         switchMap(res => {
      //                             this.getSignBlockTimes++;
      //                             if (this.toolService.checkSuccess(res.ResHeader)) {
      //                                 this.getSignBlockTimes = 0;
      //                                 this.toolService.writeSignBlock(res.Res_getSignBlock.signBlock);
      //                                 const prefix = this.toolService.getSignBlock();
      //                                 const bodyJosn = JSON.stringify(request.body);
      //                                 return next.handle(request.clone({ setHeaders: { SignCode: shajs('sha256').update(prefix + bodyJosn).digest('hex') } }));
      //                             }
      //                         })
      //                     ).subscribe();
      //                 }
      //             }
      //             // this.client.clientEventLog(this.toolService.getEventLog('AC2', result.body.ResHeader.rtnMsg)).subscribe();
      //         }
      //     }
      // }),
      // catchError((error, caught) => {
      //     if (request && request.body) {
      //         if (request.body.ReqHeader) {
      //             const txid = request.body.ReqHeader.txID;
      //             if ((txid !== TxID.refreshMemList && txid !== TxID.doJobByPk)) {
      //                 return this.handleAuthError(request, next, error);
      //             } else {
      //                 return of(error);
      //             }
      //         }
      //         else {
      //             return this.handleAuthError(request, next, error);
      //         }
      //     } else {
      //         return this.handleAuthError(request, next, error);
      //     }
      // }) as any
    );
  }

  private handleAuthError(request: HttpRequest<any>, next: HttpHandler, err: HttpErrorResponse): Observable<any> {
    //handle your auth error or rethrow
    if (err.status === 401) {
      //navigate /delete cookies or whatever
      // console.log('handled error ' + err.status);
      return this.handle401Error(request, next, err)
    }
    // else if (err.status === 404) {
    //     this.alert.ok(`HTTP error code:${err.status}`, '404 Not Found', AlertType.error);
    // }
    else {

      // http code不為200,定為AC1
      console.log('err cache',err);
      // console.log(location);
      // 跟token相關的錯誤，要抓訊息
      if (err.error.hasOwnProperty('ResHeader') && (((err.error.ResHeader.rtnCode as string).includes('9914') || (err.error.ResHeader.rtnCode as string).includes('9929')) && request.body.ReqHeader.txID != 'AA0206')) {
        return this.handleGetSignBlock(request, next);
      }
      else {
        if (err.error.hasOwnProperty('error_description')) {
          // this.alert.ok(`HTTP Status Code：${err.status}`, '', AlertType.warning, `Error：${err.error.error}<br>Description：${err.error.error_description}`);
          // console.log(location);
          if(location.pathname.indexOf('/ac4/login')>-1){
            this.translate.get(['login_fail']).subscribe(i18n => {
              this.alert.ok('', '', AlertType.warning, `<strong>${i18n.login_fail}</strong>`);
            });
          }
          else{
            this.dialogService.dialogComponentRefMap.forEach(dialog => {
              dialog.destroy();
            });
            const options: NavigationExtras  = {
              queryParams : {
                re: Date.now(),
              }
            };
            this.router.navigate(['/login'], options)

            // this.translate.get(['message.update','message.success','plz_login_again']).subscribe(i18n => {
              // this.alert.ok('', '', AlertType.warning, `<strong>${i18n.login_fail}</strong>`);
              // this.alert.logout(`${i18n['message.update']} ${i18n['message.success']}!`, `${i18n['plz_login_again']}!`);
            // });
            // this.logoutService.logout();
          }


        }
        if (err.error.hasOwnProperty('errorDescription')) {
          // 9912 : api關閉
          // tsmp custom error
          // if (err.error.ResHeader.rtnCode != '9912') {
          //     this.client.clientEventLog(this.toolService.getEventLog('AC1', err.message)).subscribe();
          // }
          if (request.body.ReqHeader.txID != 'AA0206') {
            this.alert.ok(`HTTP Status Code：${err.status}`, '', AlertType.error, `API Return Code：${err.error.ResHeader.rtnCode}<br>API Retern Message：${err.error.ResHeader.rtnMsg}`);
          }
        }
        if (err.error.hasOwnProperty('msg') && err.error.hasOwnProperty('HTTP Status')) {
          this.alert.ok(`HTTP Status Code：${err.error['HTTP Status']}`, '', AlertType.error, `API Retern Message：${err.error.msg}`);
        }
        if (!err.error.hasOwnProperty('error_description') && !err.error.hasOwnProperty('errorDescription') && (!err.error.hasOwnProperty('msg') && !err.error.hasOwnProperty('HTTP Status'))) {
          // this.alert.ok(
          //     `HTTP Status Code：${err.status}`,
          //     '',
          //     AlertType.error,
          //     `<span>message：${err.message}</span><br>
          //     <span>status：${err.status}</span><br>
          //     <span>statusText：${err.statusText}</span><br>
          //     <span>url：${err.url}</span><br>
          //     `
          // );
          let title = ''
          let content = `<strong>Http Codes：</strong><span>${err.status}</span><br><strong>Url：</strong><span>${err.url}</span><br><strong>Response：</strong><span>${JSON.stringify(err)}</span>`

          // Object.keys(err).map(key => {
          //   if (key.toLocaleLowerCase() == 'status') {
          //     title = `HTTP Status Code：${err[key]}`
          //   }
          //   if (key.toLocaleLowerCase() != 'headers' && key.toLocaleLowerCase() != 'error') {
          //     content += `<strong>${key}：</strong><span>${err[key]}</span><br>`
          //   }
          // });
          this.alert.ok(title, '', AlertType.error, content);
        }
      }

      // //跟token相關的錯誤，要抓訊息
      // if (err.url != null && err.url.includes("/oauth/token") == true) {
      //     this.alert.ok(err.error.error, err.error.error_description);
      // } else {
      //     //9912 : api關閉
      //     this.alert.ok(`HTTP error code:${err.status}`, err.message, AlertType.error);
      // }
      if(request?.body?.ReqHeader?.txID === 'AA0009'){
        this.logoutService.logout()
      }
    }
    this.ngxService.stopAll();

    throw err;

  }

  private handle401Error(req: HttpRequest<any>, next: HttpHandler, err?: HttpErrorResponse) {
    if (!this.isRefreshingToken) {
      this.isRefreshingToken = true;
      this.tokenSubject.next('');
      //refresh token api
      return this.toolService.refreshToken().pipe(
        switchMap(r => {
          if (r && r.access_token) {
            this.tokenSubject.next(r.access_token);
            return next.handle(this.addToken(req, r.access_token));
          }
          return this.logoutUser(r,true);
        }),
        catchError(error => this.logoutUser(error)),
        finalize(() => this.isRefreshingToken = false)
      );
    } else {
      return this.tokenSubject.pipe(
        tap((token:any) => {

          if (token == null ||token == '') {
            if (err) {
              // this.translate.get(['plz_login_again']).subscribe(i18n => {
              //   this.alert.ok(err.error.error, i18n.plz_login_again);
              // })
            }
            this.logoutUser(err,true);
          }
        }),
        filter(token => token != null),
        take(1),
        switchMap(token => {
          return next.handle(this.addToken(req, token));
        })
      )
    }
  }

  handleGetSignBlock(req: HttpRequest<any>, next: HttpHandler) {
    if (this.getSignBlockTimes != 3) {

      this.getSignBlockTimes++;
      this.signblockSubject.next('');
      return this.signBlockService.getSignBlock().pipe(
        switchMap(res => {
          if (this.toolService.checkSuccess(res.ResHeader)) {
            this.signblockSubject.next(res.Res_getSignBlock.signBlock);
            return next.handle(this.addSignCode(req, res.Res_getSignBlock.signBlock));
          }
          return this.logoutUser(res,true);
        }),
        catchError(err=>this.logoutUser(err)),
        finalize(() => this.getSignBlockTimes = 0)
      );
    } else {
      return this.signblockSubject.pipe(
        filter(signblock => signblock != null),
        take(1),
        switchMap(signblock => {
          return next.handle(this.addSignCode(req, signblock ?? ''));
        })
      )
    }
  }

  addToken(req: HttpRequest<any>, token: string): HttpRequest<any> {
    // console.log('add token')
    req.headers.delete('Authorization');
    return req.clone({ setHeaders: { Authorization: 'Bearer ' + token } })
  }

  addSignCode(req: HttpRequest<any>, signblock: string): HttpRequest<any> {
    req.headers.delete('SignCode');
    this.toolService.writeSignBlock(signblock);
    const bodyJosn = JSON.stringify(req.body);
    return req.clone({ setHeaders: { SignCode: shajs('sha256').update(signblock + bodyJosn).digest('hex') } });
  }

  logoutUser(err,isToken:boolean = false) {
    // console.log('isToken',isToken)
    // console.log('router',this.router)

    // if(this.router) this.logoutService.logout();
    // return throwError(() => new Error('logoutUser'));
    this.dialogService.dialogComponentRefMap.forEach(dialog => {
      dialog.destroy();
    });
    if(this.logoutService && isToken) this.logoutService.logout(isToken);
      return err;
      // throwError(() => new Error('Auth Error'));
      // throw err;
  }
}

import { LogoutService } from './logout.service';
import { ReqAA0206 } from './../../models/api/ClientService/aa0206.interface';
import { TranslateService } from '@ngx-translate/core';
import { TokenService } from './api-token.service';
import { ToolService } from './tool.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { Observable, of, throwError, empty } from 'rxjs';
import { HttpClient, HttpHeaders, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { retryWhen, switchMap, scan, takeWhile, delay, tap } from 'rxjs/operators';
import * as shajs from 'sha.js';

// import { ReqHeader, ResHeader } from 'src/app/models/api/base.header.interface';
// import { BaseRes } from 'src/app/models/api/base.interface';
import { environment } from 'src/environments/environment';
import { AlertType, TxID } from 'src/app/models/common.enum';
import * as dayjs from 'dayjs';
// import { generate } from 'generate-password';
import { NgxUiLoaderService } from 'ngx-ui-loader';
// import { BaseDotNetRes } from 'src/app/models/api/baseDotNet.interface';
// import { ReqAA0206, AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
import { Router } from '@angular/router';
import { BaseRes } from 'src/app/models/api/base.interface';
import { AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
import { BaseDotNetRes } from 'src/app/models/api/baseDotNet.interface';
import { ReqHeader } from 'src/app/models/api/base.header.interface';
// import { ReqHeader } from 'src/app/models/models/api/base.header.interface';
import * as $ from 'jquery';



@Injectable()
export class ApiBaseService {

  baseUrl: string;
  npBaseUrl: string;
  netBaseUrl: string;
  /**
   * 網路連線異常時,request retry次數
   */
  retryNums: number = 3;
  /**
   * 網路連線異常時,request delay一段時間後,繼續後送request.單位是毫秒
   */
  retryDelay: number = 2000;

  constructor(
    private httpClient: HttpClient,
    private alert: AlertService,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    private token: TokenService,
    private translate: TranslateService,
    private router: Router,
    private logoutService: LogoutService
  ) {
    this.baseUrl = environment.apiUrl;
    this.npBaseUrl = environment.dpPath;
    this.netBaseUrl = environment.netApiUrl;
  }

  private retry($observable: any): Observable<any> {
    return $observable.pipe(
      retryWhen(errors => errors.pipe(
        switchMap((error) => {
          return of(error.status);
          //   return _throw({message: error.error.message || 'Notification.Core.loginError'});
        }),
        scan((acc) => {
          return acc + 1;
        }, 0),
        takeWhile(acc => acc < this.retryNums),
        delay(this.retryDelay),
      ))
    )
  }

  cryptSignCode(body: Object): string {
    // const prefix = 'DzANBgNVBAgTBnRhaXdhbjEPMA0GA1UEBxMGdGFpcGVpMRMwEQYDVQQKEwp0aGlu';
    const prefix = this.toolService.getSignBlock();
    const bodyJosn = JSON.stringify(body);
    return shajs('sha256').update(prefix + bodyJosn).digest('hex');
  }

  public get<T>(url: string, params?: HttpParams, headers?: HttpHeaders): Observable<T> {
    let $object = this.httpClient.get(`${this.baseUrl}/${url}`, { headers: headers, params: params })
    return this.retry($object)
  }

  public excutePost<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[], completeUrl = ''): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`,
      });
      let url = '';
      if (completeUrl)
        url = completeUrl;
      else
        url = `${this.baseUrl}/${path}`;
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(url, body, { headers: header }).pipe(
        tap(r => {
          //0158 api已存在
          if (!rtnCodes) {
            if (r.ResHeader.rtnCode != '0000') {
              let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg);//判斷如不為0000 , show rtnMsg
              }
            }
          } else {
            if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '0000' && r.ResHeader.rtnCode != '1100') {
              let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg);//判斷如不為0000 , show rtnMsg
              }
            }
          }
          this.ngxService.stopAll();
        }),
        // catchError(this.handleError)
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  /* 無論rtnCode為何皆不跳提示窗*/
  public excutePost_bg<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[], completeUrl = ''): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`
      });
      let url = '';
      if (completeUrl)
        url = completeUrl;
      else
        url = `${this.baseUrl}/${path}`;
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(url, body, { headers: header }).pipe(
        tap(r => {
          if (!rtnCodes) {
            if (r.ResHeader.rtnCode != '0000') {
              let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              // if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
              //     this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg, null);//判斷如不為0000 , show rtnMsg
              // }
            }
          } else {
            if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '0000' && r.ResHeader.rtnCode != '1100') {
              let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              // if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
              //     this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg, null);//判斷如不為0000 , show rtnMsg
              // }
            }
          }
          this.ngxService.stopAll();
        }),
        // catchError(this.handleError)
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDotNetPost<T extends BaseDotNetRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`,
        'backAuth': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.baseUrl}/${path}`, body, { headers: header }).pipe(
        // let $object = this.httpClient.post<T>(`http://192.168.1.156:48083/${path}`, body, { headers: header }).pipe(
        tap(r => {
          //0158 api已存在
          if (!rtnCodes) {
            if (r.resHeader.rtnCode != '0000') {
              let req_0206 = this.toolService.getEventLog('AC2', r.resHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              if (!(r.resHeader.rtnCode as string).includes('9914') && !(r.resHeader.rtnCode as string).includes('9929')) {
                this.alert.ok(`Error code :${r.resHeader.rtnCode}`, r.resHeader.rtnMsg);//判斷如不為0000 , show rtnMsg
              }
            }
          } else {
            if (!rtnCodes.includes(r.resHeader.rtnCode) && r.resHeader.rtnCode != '0000') {
              let req_0206 = this.toolService.getEventLog('AC2', r.resHeader.rtnMsg) as AA0206Req;
              this.eventLog(req_0206);
              if (!(r.resHeader.rtnCode as string).includes('9914') && !(r.resHeader.rtnCode as string).includes('9929')) {
                this.alert.ok(`Error code :${r.resHeader.rtnCode}`, r.resHeader.rtnMsg);//判斷如不為0000 , show rtnMsg
              }
            }
          }
          this.ngxService.stopAll();
        }),
        // catchError(this.handleError)
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteSignBlockGet<T extends BaseRes>(path: string, token: string = ""): Observable<T> {
    return new Observable(obser => {
      if (token == "") { token = this.toolService.getToken()!; }
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.get<T>(`${this.baseUrl}/${path}`, { headers: header }).pipe(
        tap(r => {
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      });
    });
  }

  public excuteFileUpload<T extends BaseRes>(path: string, file: File, rtnCodes?: string[]): Observable<T> {
    return new Observable(obser => {
      let serverno = '1';
      let d = new Date();
      let date = dayjs(d).format('YYMMDDHHmmss');
      // let alphaNumber = generate({ length: 6, numbers: true });
      let alphaNumber = this.generateRandomString(6);
      let txDate = this.toolService.formateDate(d);
      let formData = new FormData();
      formData.append('txSN', `${serverno}${date}${alphaNumber}`);
      formData.append('txDate', txDate);
      formData.append('txID', 'DPB0082');
      formData.append('cID', sessionStorage.getItem('decode_token') ? JSON.parse(sessionStorage.getItem('decode_token')!)['client_id'] : '');
      formData.append('file', file);
      let token = this.toolService.getToken();
      // let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        // 'Content-Type': 'application/json'
        // 'SignCode': signCode,
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.baseUrl}/${path}`, formData, { headers: header }).pipe(
        tap(r => {
          if (!rtnCodes) {
            if (r.ResHeader) {
              // Digirunner APIM底層Error
              if (r.ResHeader.rtnCode != '1100') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                }
                else {
                  let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                  this.eventLog(req_0206);
                  if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                    this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                  }
                }
                this.ngxService.stopAll();
              }
            }
          } else {
            if (r.ResHeader) {
              if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '1100') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                }
                this.ngxService.stopAll();
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      });
    });
  }

  excuteNpPost_ignoreAll<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`,
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.npBaseUrl}/${path}`, body, { headers: header }).pipe(
        // let $object = this.httpClient.post<T>(`/${path}`, body, { headers: header }).pipe(
        tap(r => {
          if (!rtnCodes) {
            if (r.ResHeader) {
              // Digirunner APIM底層Error
              if (r.ResHeader.rtnCode != '1100' && r.ResHeader.rtnCode != '1298' && r.ResHeader.rtnCode != '1297') {
                // let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                // this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                  if (r.ResHeader.rtnCode == '0001') {
                    if (this.router) this.logoutService.logout();;
                  }
                }
                this.ngxService.stopAll();
              }
            }
          } else {
            if (r.ResHeader) {
              if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '1100' && r.ResHeader.rtnCode != '1298' && r.ResHeader.rtnCode != '1297') {
                // let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                // this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                  if (r.ResHeader.rtnCode == '0001') {
                    if (this.router) this.logoutService.logout();
                  }
                }
                this.ngxService.stopAll();
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteNpPost_ignore1298<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`,
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.npBaseUrl}/${path}`, body, { headers: header }).pipe(
        // let $object = this.httpClient.post<T>(`/${path}`, body, { headers: header }).pipe(
        tap(r => {
          if (!rtnCodes) {
            if (r.ResHeader) {
              // Digirunner APIM底層Error
              if (r.ResHeader.rtnCode != '1100' && r.ResHeader.rtnCode != '1298') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                  if (r.ResHeader.rtnCode == '0001') {
                    if (this.router) this.logoutService.logout();;
                  }
                }
                this.ngxService.stopAll();
              }
            }
          } else {
            if (r.ResHeader) {
              if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '1100' && r.ResHeader.rtnCode != '1298') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                  if (r.ResHeader.rtnCode == '0001') {
                    if (this.router) this.logoutService.logout();
                  }
                }
                this.ngxService.stopAll();
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }


  public excuteNpPost<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return new Observable( obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`,
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.npBaseUrl}/${path}`, body, { headers: header }).pipe(
        // let $object = this.httpClient.post<T>(`/${path}`, body, { headers: header }).pipe(
        tap(async (r) => {
          if (!rtnCodes) {
            if (r.ResHeader) {
              // Digirunner APIM底層Error
              if (r.ResHeader.rtnCode != '1100') {
                // console.log(r.ResHeader.rtnMsg);
                if (r.ResHeader.rtnMsg.includes('[{{') && r.ResHeader.rtnMsg.includes('}}]')) {
                  let _rtnMsg = r.ResHeader.rtnMsg;
                  while (_rtnMsg.includes('{{') && _rtnMsg.includes('}}')) {
                    let _msg = _rtnMsg;
                    let formControlName = _msg.substring(_msg.indexOf('{{') + 2, _msg.indexOf('}}'));
                    // console.log('form control name :', formControlName)
                    if (document.getElementById(`${formControlName}_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_label`).text()) : _msg;
                    }
                    else if (document.getElementById(`${formControlName}_page4_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_page4_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_page4_label`).text()) : _msg;
                    }
                    else if (document.getElementById(`${formControlName}_page9_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_page9_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_page9_label`).text()) : _msg;
                    }
                    else { // 預防HTML沒有相對應的label
                      const code = [formControlName];
                      const dict = await this.toolService.getDict(code);
                      // console.log(dict);
                      // this.translate.get(codes).subscribe(dict => {
                      _rtnMsg = _msg.replace(`{{${formControlName}}}`, `${dict[formControlName]}`);
                      // console.log(_rtnMsg)
                      // })
                      break;
                    }
                  }
                  let req_0206 = this.toolService.getEventLog('AC2', _rtnMsg) as AA0206Req;
                  this.eventLog(req_0206);
                  if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                    // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, _rtnMsg); // 判斷不為1100 , show rtnMsg
                    // this.alert.ok(r.ResHeader.rtnMsg,'' , AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID);
                    this.alert.ok(_rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID);
                  }
                }
                else {
                  let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                  this.eventLog(req_0206);
                  if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                    // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                    this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg
                    if (r.ResHeader.rtnCode == '0001') {
                      if (this.router) this.logoutService.logout();
                    }
                  }
                }
                this.ngxService.stopAll();
              }
            }
          } else {
            if (r.ResHeader) {
              if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '1100') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                }
                this.ngxService.stopAll();
              }
            }
          }
        })
      )

      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDpPut<T extends BaseRes>(path: string, body?: any): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.put<T>(`${this.baseUrl}/${path}`, body, { headers: header }).pipe(
        tap(r => {
          if (r.ResHeader) {
            // Digirunner APIM底層Error
            this.ngxService.stopAll();
            if (r.ResHeader.rtnCode != '1100') {
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDpGet<T extends BaseRes>(path: string, txID: TxID): Observable<T> {

    return new Observable(obser => {
      let token = this.toolService.getToken();
      let obj: { [k: string]: any } = {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      };
      const reqheader: ReqHeader = this.getReqHeader(txID);
      Object.keys(reqheader).forEach(key => {
        // header.set(key , reqheader[key]);
        obj[key] = reqheader[key];
      });
      let header = new HttpHeaders(obj);
      this.toolService.setExpiredTime();
      let $object = this.httpClient.get<T>(`${this.baseUrl}/${path}`, { headers: header }).pipe(
        tap(r => {
          if (r.ResHeader) {
            // Digirunner APIM底層Error
            this.ngxService.stopAll();
            if (r.ResHeader.rtnCode != '1100') {
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                //若回傳資料rtnMsg內含'b64'則解碼後呈現 -20210715
                let rtnMsgArr = r.ResHeader.rtnMsg.split('b64:');
                console.log(rtnMsgArr);
                if (rtnMsgArr.length > 1) {
                  let rtnMsgB64decode = r.ResHeader.rtnMsg + "<br><textarea class='form-control' style='margin-top:10%;width:100%;min-height:200px;'>" + this.toolService.Base64Decoder(rtnMsgArr[1].substr(0, rtnMsgArr[1].length - 1)) + "</textarea>";
                  //  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg, undefined , rtnMsgB64decode); // 判斷後兩碼如不為00 , show rtnMs
                  this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID + '<br>' + rtnMsgB64decode); // 判斷不為1100 , show rtnMsg

                }
                else {
                  // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                  this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg
                }


              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDpGetFile(path: string): Observable<Blob> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let tmpHeader = this.getReqHeader(TxID.downloadFile);

      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      });

      Object.keys(tmpHeader).forEach(key => {
        header = header.append(key, tmpHeader[key]);

      })

      // return;
      this.toolService.setExpiredTime();

      let $object = this.httpClient.get(`${this.baseUrl}/${path}`, { headers: header, responseType: 'blob' }).pipe(
        tap(r => {
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excutePostGetFile(path: string, body?: any): Observable<Blob> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post(`${this.baseUrl}/${path}`, body, { headers: header, responseType: 'blob' }).pipe(
        tap(r => {
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDpGetPEMFile(path: string, body?: any): Observable<Blob> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        'Content-Type': 'application/json',
        'SignCode': signCode,
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post(`${this.baseUrl}/${path}`, body, { headers: header, responseType: 'blob' }).pipe(
        tap(r => {
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      });
    });
  }

  public excuteDpDelete<T extends BaseRes>(path: string, primarykey: number): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      const options = {
        headers: new HttpHeaders({
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }),
        body: {
          'primarykey': primarykey
        },
      };

      // console.log(options);
      this.toolService.setExpiredTime();
      let $object = this.httpClient.delete<T>(`${this.baseUrl}/${path}`, options).pipe(
        tap(r => {
          if (r.ResHeader) {
            // Digirunner APIM底層Error
            this.ngxService.stopAll();
            if (r.ResHeader.rtnCode != '1100') {
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg


              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  public excuteDpUpload<T extends BaseRes>(path: string, body?: any): Observable<T> {
    return new Observable(obser => {
      let token = this.toolService.getToken();
      let header = new HttpHeaders({
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.baseUrl}/${path}`, body, { headers: header }).pipe(
        tap(r => {
          if (r.ResHeader) {
            // Digirunner APIM底層Error
            this.ngxService.stopAll();
            if (r.ResHeader.rtnCode != '1100') {
              if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      })
    })
  }

  /**
   *
   * @param path 路徑
   * @param body 資料
   * @param rtnCodes 表示傳入的這些錯誤代碼不由底層處理，底層不攔截．傳入的rtnCode不可為0000
   */
  public post<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    //預先埋好，如果後端無法解決401問題，這段再打開
    // if (this.toolService.isTokenExpired()) {
    //     return this.toolService.refreshToken().pipe(
    //         concatMap(() => this.excutePost(path, body))
    //     );
    // } else {
    return this.excutePost(path, body, rtnCodes)
    // }
  }

  /**
   *
   * @param path 路徑
   * @param body 資料
   * @param rtnCodes 表示傳入的這些錯誤代碼不由底層處理，底層不攔截．傳入的rtnCode不可為0000
   */
  public npPost<T extends BaseRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return this.excuteNpPost(path, body, rtnCodes)
  }

  public dotNetPost<T extends BaseDotNetRes>(path: string, body?: any, rtnCodes?: string[]): Observable<T> {
    return this.excuteDotNetPost(path, body, rtnCodes)
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      // this.tokenService.auth('','')
      console.error(
        `Backend returned code ${error.status}, ` +
        `body was: ${JSON.stringify(error.error)}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  };

  eventLog<T extends BaseRes>(ReqBody: AA0206Req) {
    // http code 200, 但 rtnCode 不為 0000，定為 AC2
    ReqBody.agent = ReqBody.agent.replace(/[;]/g, ',');
    let body = {
      ReqHeader: this.getReqHeader(TxID.clientEventLog),
      ReqBody: ReqBody
    } as ReqAA0206
    if (!body.ReqBody.isLogin) return empty();
    let token = this.toolService.getToken();
    let signCode = this.cryptSignCode(body);
    let header = new HttpHeaders({
      'Content-Type': 'application/json',
      'SignCode': signCode,
      'Authorization': `Bearer ${token}`
    });
    const moduleName = environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    const path = `${this.npBaseUrl}/${moduleName}/AA0206`;
    this.httpClient.post<T>(path, body, { headers: header }).subscribe();
    return null;
  }

  getReqHeader(txID: TxID) {
    /**
     * txSN 格式為 ServerNo(1) + Date(yyMMddHHmmss) + AlphaNumber(6)
     * ex. "1180823173301000001" (不可重複)
     * (ServerNo + 2018/8/23 17:33:01 + 最後6碼可英數字)"
     */
    //ServerNo 固定為1
    let serverno = '1';
    let d = new Date();
    let date = dayjs(d).format('YYMMDDHHmmss');
    // let alphaNumber = generate({ length: 6, numbers: true });
    let alphaNumber = this.generateRandomString(6);

    let txDate = this.toolService.formateDate(d);
    let _locale = this.toolService.getLocale().split('-');
    return {
      txSN: `${serverno}${date}${alphaNumber}`,
      txDate: txDate,
      txID: txID,
      cID: sessionStorage.getItem('decode_token') ? JSON.parse(sessionStorage.getItem('decode_token')!)['client_id'] : '',
      locale: _locale.length>1 ? `${_locale[0].toLowerCase()}-${_locale[1]}` : `${_locale[0].toLowerCase()}`
    } as ReqHeader;
  }

  generateRandomString(strLength: number) {
    var numberChars = "0123456789";
    var upperChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    var lowerChars = "abcdefghijklmnopqrstuvwxyz";
    var allChars = numberChars + upperChars + lowerChars;
    var randPasswordArray = Array(strLength);
    randPasswordArray[0] = numberChars;
    randPasswordArray[1] = upperChars;
    randPasswordArray[2] = lowerChars;
    randPasswordArray = randPasswordArray.fill(allChars, 3);
    return this.shuffleArray(randPasswordArray.map(function (x) { return x[Math.floor(Math.random() * x.length)] })).join('');
  }

  shuffleArray(array: any[]) {
    for (var i = array.length - 1; i > 0; i--) {
      var j = Math.floor(Math.random() * (i + 1));
      var temp = array[i];
      array[i] = array[j];
      array[j] = temp;
    }
    return array;
  }

  excuteTsmpSetting<T extends BaseRes>(path: string, file: File, req: any, rtnCodes?: string[]): Observable<T> {
    return new Observable(obser => {
      // let serverno = '1';
      // let d = new Date();
      // let date = dayjs(d).format('YYMMDDHHmmss');
      // let alphaNumber = generate({ length: 6, numbers: true });
      // let alphaNumber = this.generatePassword(6);
      // let txDate = this.toolService.formateDate(d);
      let formData = new FormData();
      // formData.append('txSN', `${serverno}${date}${alphaNumber}`);
      // formData.append('txDate', txDate);
      // formData.append('txID', 'DPB9922');
      // formData.append('cID', sessionStorage.getItem('decode_token') ? JSON.parse(sessionStorage.getItem('decode_token')!)['client_id'] : '');
      formData.append('file', file);
      formData.append('req', JSON.stringify(req));

      let token = this.toolService.getToken();
      // let signCode = this.cryptSignCode(body);
      let header = new HttpHeaders({
        // 'Content-Type': 'application/json',
        // 'SignCode': signCode,
        'Authorization': `Bearer ${token}`
      });
      this.toolService.setExpiredTime();
      let $object = this.httpClient.post<T>(`${this.baseUrl}/${path}`, formData, { headers: header }).pipe(
        tap(r => {
          if (!rtnCodes) {
            if (r.ResHeader) {
              // Digirunner APIM底層Error
              if (r.ResHeader.rtnCode != '1100') {
                if (r.ResHeader.rtnMsg.includes('[{{') && r.ResHeader.rtnMsg.includes('}}]')) {
                  let _rtnMsg = r.ResHeader.rtnMsg;
                  while (_rtnMsg.includes('{{') && _rtnMsg.includes('}}')) {
                    let _msg = _rtnMsg;
                    let formControlName = _msg.substring(_msg.indexOf('{{') + 2, _msg.indexOf('}}'));
                    console.log('form control name :', formControlName)

                    if (document.getElementById(`${formControlName}_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_label`).text()) : _msg;
                    }
                    else if (document.getElementById(`${formControlName}_page4_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_page4_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_page4_label`).text()) : _msg;
                    }
                    else if (document.getElementById(`${formControlName}_page9_label`)) {
                      _rtnMsg = document.getElementById(`${formControlName}_page9_label`) ? _msg.replace(`{{${formControlName}}}`, $(`#${formControlName}_page9_label`).text()) : _msg;
                    }
                    else { // 預防HTML沒有相對應的label
                      const codes = [formControlName];
                      this.translate.get(codes).subscribe(dict => {
                        _rtnMsg = _msg.replace(`{{${formControlName}}}`, `${dict[formControlName]}`)
                      })
                      break;
                    }
                  }
                  let req_0206 = this.toolService.getEventLog('AC2', _rtnMsg) as AA0206Req;
                  this.eventLog(req_0206);
                  if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                    // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, _rtnMsg); // 判斷不為1100 , show rtnMsg
                    // this.alert.ok(r.ResHeader.rtnMsg,'' , AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID);
                    this.alert.ok(_rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID);
                  }
                }
                else {
                  let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                  this.eventLog(req_0206);
                  if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                    // this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷不為1100 , show rtnMsg
                    this.alert.ok(r.ResHeader.rtnMsg, '', AlertType.warning, r.ResHeader.txDate + '<br>' + r.ResHeader.txID); // 判斷不為1100 , show rtnMsg
                    if (r.ResHeader.rtnCode == '0001') {
                      if (this.router) this.logoutService.logout();
                    }
                  }
                }
                this.ngxService.stopAll();
              }
            }
          } else {
            if (r.ResHeader) {
              if (!rtnCodes.includes(r.ResHeader.rtnCode) && r.ResHeader.rtnCode != '1100') {
                let req_0206 = this.toolService.getEventLog('AC2', r.ResHeader.rtnMsg) as AA0206Req;
                this.eventLog(req_0206);
                if (!(r.ResHeader.rtnCode as string).includes('9914') && !(r.ResHeader.rtnCode as string).includes('9929')) {
                  this.alert.ok(`Return code : ${r.ResHeader.rtnCode}`, r.ResHeader.rtnMsg); // 判斷後兩碼如不為00 , show rtnMsg
                }
                this.ngxService.stopAll();
              }
            }
          }
        })
      )
      $object.subscribe(r => {
        obser.next(r);
      });
    });
  }

}

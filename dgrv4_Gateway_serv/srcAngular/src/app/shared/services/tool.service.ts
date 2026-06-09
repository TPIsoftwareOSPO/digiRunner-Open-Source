import { generate } from 'generate-password';
import { LdapEnvItem } from './../../models/api/LoginService/ldaplogin.interface';
import { AA0311Req } from './../../models/api/ApiService/aa0311_v3.interface';
import { from, Subject } from 'rxjs';
import { TimeRange } from './../../models/common.enum';
import { TokenService } from './api-token.service';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, Injector } from '@angular/core';
// import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { GrantType } from 'src/app/models/common.enum';
import { concatMap, tap, catchError, map } from 'rxjs/operators';
import { Observable, of } from 'rxjs';
// import { ResHeader } from 'src/app/models/api/base.header.interface';
import { FormGroup } from '@angular/forms';
// import { AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
// import * as aesjs from "aes-js";
import { isNumber } from 'util';
import { environment } from 'src/environments/environment';
import { ResToken } from 'src/app/models/api/TokenService/token.interface';
import { AA0206Req } from 'src/app/models/api/ClientService/aa0206.interface';
import * as dayjs from 'dayjs';
import * as utc from 'dayjs/plugin/utc';
import * as timezone from 'dayjs/plugin/timezone';
import { ResHeader } from 'src/app/models/api/base.header.interface';
import { AA0101func } from 'src/app/models/api/FuncService/aa0101.interface';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { TranslateService } from '@ngx-translate/core';
// import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import * as bcrypt from 'bcryptjs';
import { Router } from '@angular/router';
import { Menu } from 'src/app/models/menu.model';
import * as base64 from 'js-base64';
// import { AA0101func } from 'src/app/models/api/FuncService/aa0101.interface';
// import { LdapEnvItem } from 'src/app/models/api/LoginService/ldaplogin.interface';
import { routes } from 'src/app/layout/layout.routing';

export const TOKEN = 'access_token';
import { jwtDecode } from 'jwt-decode';

@Injectable()
export class ToolService {
  /*
   * 每當執行Api則更新此參數
   */
  expiredTimeEvt = new Subject();

  /**清除閒置時間倒數*/
  clearExpiredTimeout = new Subject();

  /*自定義base64/base64url encoder and decoder */
  b64c = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/'; // base64 dictionary
  b64u = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_'; // base64url dictionary
  b64pad = '=';

  constructor(
    private tokenService: TokenService,
    private translate: TranslateService,
  ) // private jwtHelper: JwtHelperService,
  // public router: Router
  {
    dayjs.extend(utc);
    dayjs.extend(timezone);
  }

  // private get router():Router{
  //     return this.injector.get(Router);
  // }

  Base64Encoder(str: string) {
    return window.btoa(unescape(encodeURIComponent(str))); // For URL Encoder UTF-8 convert
    // return window.btoa(str);
  }

  Base64Decoder(base64str: string) {
    return decodeURIComponent(escape(window.atob(base64str))); // For URL Encoder UTF-8 convert
    // return window.atob(base64str);
  }

  /*
   *  自定義base64/base64url encoder and decoder
   *  參考source https://simplycalc.com/base64-source.php
   *  開始---------------------
   */
  base64_encode_data(data: string, len: number, b64x: string) {
    let dst: string = '';
    let i: number = 0;

    for (i = 0; i <= len - 3; i += 3) {
      dst += b64x.charAt(data.charCodeAt(i) >>> 2);
      dst += b64x.charAt(
        ((data.charCodeAt(i) & 3) << 4) | (data.charCodeAt(i + 1) >>> 4)
      );
      dst += b64x.charAt(
        ((data.charCodeAt(i + 1) & 15) << 2) | (data.charCodeAt(i + 2) >>> 6)
      );
      dst += b64x.charAt(data.charCodeAt(i + 2) & 63);
    }

    if (len % 3 == 2) {
      dst += b64x.charAt(data.charCodeAt(i) >>> 2);
      dst += b64x.charAt(
        ((data.charCodeAt(i) & 3) << 4) | (data.charCodeAt(i + 1) >>> 4)
      );
      dst += b64x.charAt((data.charCodeAt(i + 1) & 15) << 2);
      dst += this.b64pad;
    } else if (len % 3 == 1) {
      dst += b64x.charAt(data.charCodeAt(i) >>> 2);
      dst += b64x.charAt((data.charCodeAt(i) & 3) << 4);
      dst += this.b64pad;
      dst += this.b64pad;
    }

    return dst;
  }

  /* base64_encode
   * Encode a JavaScript string to base64.
   * Specified string is first converted from JavaScript UCS-2 to UTF-8.
   */
  base64_encode(str: string) {
    var utf8str = unescape(encodeURIComponent(str));
    return this.base64_encode_data(utf8str, utf8str.length, this.b64c);
  }

  /* base64url_encode
   * Encode a JavaScript string to base64url.
   * Specified string is first converted from JavaScript UCS-2 to UTF-8.
   */
  base64url_encode(str) {
    var utf8str = unescape(encodeURIComponent(str));
    return this.base64_encode_data(utf8str, utf8str.length, this.b64u);
  }

  /* base64_charIndex
   * Internal helper to translate a base64 character to its integer index.
   */
  base64_charIndex(c: string) {
    if (c == '+') return 62;
    if (c == '/') return 63;
    return this.b64u.indexOf(c);
  }

  /* base64_decode
   * Decode a base64 or base64url string to a JavaScript string.
   * Input is assumed to be a base64/base64url encoded UTF-8 string.
   * Returned result is a JavaScript (UCS-2) string.
   */
  base64_decode(data: string) {
    let dst: string = '';
    let i: number, a: number, b: number, c: number, d: number, z: number;

    for (i = 0; i < data.length - 3; i += 4) {
      a = this.base64_charIndex(data.charAt(i + 0));
      b = this.base64_charIndex(data.charAt(i + 1));
      c = this.base64_charIndex(data.charAt(i + 2));
      d = this.base64_charIndex(data.charAt(i + 3));

      dst += String.fromCharCode((a << 2) | (b >>> 4));
      if (data.charAt(i + 2) != this.b64pad)
        dst += String.fromCharCode(((b << 4) & 0xf0) | ((c >>> 2) & 0x0f));
      if (data.charAt(i + 3) != this.b64pad)
        dst += String.fromCharCode(((c << 6) & 0xc0) | d);
    }

    dst = decodeURIComponent(escape(dst));
    return dst;
  }

  /* base64url_sniff
   * Check whether specified base64 string contains base64url specific characters.
   * Return true if specified string is base64url encoded, false otherwise.
   */
  base64url_sniff(base64) {
    if (base64.indexOf('-') >= 0) return true;
    if (base64.indexOf('_') >= 0) return true;
    return false;
  }

  /*結束--------------------------------------*/

  BcryptEncoder(item_no: string) {
    // SELECT * FROM (SELECT ITEM_NO, MIN(SORT_BY) as SORT_BY FROM TSMP_DP_ITEMS group by ITEM_NO) t order by t.SORT_BY;
    return bcrypt.hashSync(item_no, 4);
  }

  // getLanguageCodes() {
  //     if (environment.production) {
  //         let promise = $.get(`../${environment.subPath}assets/json/language-codes.json`).promise();
  //         return this.http.get(promise);
  //     }
  //     else {
  //         let promise = $.get('../assets/json/language-codes.json').promise();
  //         return from(promise);
  //     }
  // }

  setUserID(userid: string) {
    sessionStorage.setItem('user_id', userid);
  }

  setUserAlias(userAlias: string) {
    sessionStorage.setItem('user_alias', userAlias);
  }

  getUserID() {
    return sessionStorage.getItem('user_id') ?? '';
  }

  getUserAlias() {
    return sessionStorage.getItem('user_alias') ?? '';
  }

  getUserName() {
    const userName = sessionStorage.getItem('decode_token')
      ? JSON.parse(sessionStorage.getItem('decode_token')!)['user_name']
      : '';
    return userName;
  }

  getOrgId() {
    const org_id = sessionStorage.getItem('decode_token')
      ? JSON.parse(sessionStorage.getItem('decode_token')!)['org_id']
      : '';
    return org_id;
  }

  getAuthorities(): Array<string> {
    let decode_token = sessionStorage.getItem('decode_token');
    if (decode_token) return JSON.parse(decode_token)['authorities'];
    else return [];
  }

  setTokenInfo(r: ResToken) {
    sessionStorage.setItem('expires_in', r.expires_in.toString());
    sessionStorage.setItem('jti', r.jti);
    sessionStorage.setItem('node', r.node);
    sessionStorage.setItem('refresh_token', r.refresh_token);
    sessionStorage.setItem('scope', r.scope);
    sessionStorage.setItem('token_type', r.token_type);
  }

  decodeToken() {
    return jwtDecode(sessionStorage.getItem(TOKEN)!);
  }

  isTokenExpired(token: string = TOKEN): boolean {
    let jwtStr = sessionStorage.getItem(token) ?? undefined;
    if (jwtStr) {
      return this.checkTokenExpired(jwtStr); // token expired?
    } else {
      return true; // no token
    }
  }

  checkTokenExpired(token: string): boolean {
  try {
    const decoded: any = jwtDecode(token);

    // 如果 token 裡根本沒有 exp 欄位，通常視為永不過期或格式有誤
    if (!decoded.exp) return false;

    // 轉換成毫秒並與現在時間比較
    // Date.now() / 1000 得到當前秒數
    return decoded.exp < Date.now() / 1000;
  } catch (err) {
    // 解碼失敗（例如格式錯誤）通常視為已過期或無效
    return true;
  }
}

  refreshToken() {
    return this.tokenService.auth('', '', GrantType.refresh_token).pipe(
      concatMap((r) => {
        this.setTokenInfo(r);
        this.writeToken(r.access_token);
        this.writeToken(JSON.stringify(this.decodeToken()), 'decode_token');
        return of(r);
      })
    );
  }

  writeToken(value: string, token: string = TOKEN) {
    sessionStorage.setItem(token, value);
  }

  getSignBlock(): string {
    return sessionStorage.getItem('signBlock') ?? '';
  }

  writeSignBlock(block: string) {
    sessionStorage.setItem('signBlock', block);
  }

  writeAcConf(acConf: string): void {
    sessionStorage.setItem('AcConf', acConf);
  }

  getAcConf() {
    // console.log(sessionStorage.getItem("AcConf"))
    // return sessionStorage.getItem("AcConf") ? JSON.parse(sessionStorage.getItem("AcConf")!) as AA0510Resp : '';
    return sessionStorage.getItem('AcConf')
      ? JSON.parse(sessionStorage.getItem('AcConf')!)
      : '';
  }

  // write(key: string, value: string) {
  //     sessionStorage.setItem(key, value);
  // }

  // get(key: string): string {
  //     return sessionStorage.getItem(key);
  // }

  // clear(key: string): void {
  //     sessionStorage.removeItem(key);
  // }

  // getToken(token: string = TOKEN): Observable<any> {

  //     return new Observable(obser => {
  //     // return Observable.arguments(obser => {
  //         console.log('token is expired:', this.isTokenExpired());
  //         if (this.isTokenExpired()) {
  //             this.tokenService.auth('', '', GrantType.refresh_token).pipe(
  //                 tap(r => {
  //                     this.setTokenInfo(r);
  //                     this.writeToken(r.access_token);
  //                     this.writeToken(JSON.stringify(this.decodeToken()), "decode_token");
  //                 }),
  //                 catchError(() => {
  //                     this.route.navigate(['/login']);
  //                     return '';
  //                 })
  //             ).subscribe(r => {
  //                 obser.next(r)
  //             })
  //         } else {
  //             obser.next(sessionStorage.getItem(token));
  //         }

  //     })
  // }

  getToken() {
    return sessionStorage.getItem('access_token') ?? '';
  }

  getDecodeToken() {
    return jwtDecode(sessionStorage.getItem('access_token') ?? '');
  }

  removeAll() {
    sessionStorage.clear();
  }

  deleteProperties(
    object: { [key: string]: string | undefined },
    exceptions?: string[]
  ) {
    for (var key in object) {
      if (object.hasOwnProperty(key)) {
        if (exceptions && exceptions.includes(key)) {
          continue;
        } else {
          if (
            object[key] === null ||
            object[key] == undefined ||
            object[key] === ''
          )
            delete object[key];
        }
      }
    }
    return object;
  }

  // private clearString(s) {
  //     var pattern = new RegExp("[`~!@#$^&*()=|{}':;',\\[\\].<>/?~！@#￥……&*（）&;|{}【】‘；：”“'。，、？-]")
  //     var rs = "";
  //     for (var i = 0; i < s.length; i++) {
  //         rs = rs + s.substr(i, 1).replace(pattern, '');
  //     }
  //     return rs;
  // }

  //   convertStringToUtcDate(utcString: string) {
  //       //20180812T173301+0800
  //       if (!utcString) return;
  //       if (utcString.length != 20) return;
  //       let position = 0
  //       let YYYY = utcString.substr(0, 4);
  //       let MM = utcString.substr(4, 2);
  //       let DD = utcString.substr(6, 2);
  //       let HH = utcString.substr(9, 2);
  //       let mm = utcString.substr(11, 2);
  //       let ss = utcString.substr(13, 2);
  //       let utc = utcString.slice(15);
  //       utc = utc.slice(0, 3) + ':' + utc.slice(3);
  //       // let newDate = dayjs(`${YYYY}-${MM}-${DD} ${HH}:${mm}:${ss}${utc}`).toDate();
  //       let newDate = new Date(`${YYYY}-${MM}-${DD}T${HH}:${mm}:${ss}`);
  //       return newDate;
  //       // return dayjs(newDate).format('YYYY-MM-DD HH:mm:ss');
  //   }

  formateDate(date: Date): string {
    let TZD = date.toString().match(/([-\+][0-9]+)\s/)![1];
    return dayjs(date).format(`YYYYMMDD[T]HHmmss[${TZD}]`);
  }

  setformate(date: Date, format?: string): string {
    return date ? dayjs(date).format(format) : '';
  }

  // formateUTCData(date:Date):string {
  // return dayjs(date).tz()
  // }
  getTimeZone() {
    return dayjs.tz.guess();
  }
  //   calcEndTime(date: Date) {
  //       return dayjs(date).add(1, 'day').subtract(1, 'second').toDate()
  //   }

  addYear(date: Date, num: number) {
    return dayjs(date).add(num, 'year').toDate();
  }

  addDay(date: Date, num: number) {
    return dayjs(date).add(num, 'day').toDate();
  }

  addMonth(date: Date, num: number) {
    return dayjs(date).add(num, 'month').toDate();
  }

  getLocale() {
    let defaultLang = (navigator.language || 'en-us').toUpperCase();
    // console.log(defaultLang)
    if (defaultLang == 'ZH') defaultLang = 'ZH-TW';
    if (defaultLang == 'EN') defaultLang = 'EN-US';

    return defaultLang;
  }

  // getLocaleName() {
  //   let localLang = '';
  //   if (navigator.language.includes('en')) {
  //     localLang = 'en-US';
  //   }
  //   else if (navigator.language.includes('zh')) {
  //     localLang = 'zh-TW';
  //   }
  //   else {
  //     localLang = 'en-US';
  //   }
  //   return localLang;
  // }

  checkSuccess(resHeader: ResHeader) {
    return resHeader.rtnCode === '0000';
  }

  checkDpSuccess(resHeader: ResHeader) {
    return resHeader.rtnCode === '1100';
  }

  writeRoleFuncCodeList(funcCodeList: Array<string>) {
    sessionStorage.setItem('roleFuncCodeList', funcCodeList.toString());
  }

  getRoleFuncCodeList(): Array<string> {
    return sessionStorage.getItem('roleFuncCodeList')
      ? sessionStorage.getItem('roleFuncCodeList')!.split(',')
      : [];
  }

  setFuncList(funcDetailList: Array<AA0101func>): any {
    sessionStorage.setItem('func_list', JSON.stringify(funcDetailList));
  }

  // getFuncList(): Array<AA0101func> {
  getFuncList() {
    return sessionStorage.getItem('func_list')
      ? JSON.parse(sessionStorage.getItem('func_list')!)
      : undefined;
  }

  //   /**
  //  * 重置form表單
  //  * @param key_filer 不要重置的control names
  //  */
  //   resetForm(form: FormGroup, key_filer: string[] = []): Observable<any> {
  //       return Observable.create(obser => {
  //           form.reset();
  //           Object.keys(form.controls).forEach(key => {
  //               if (!key_filer.includes(key)) form.get(key).setErrors({ 'invalid': true });
  //           });
  //           obser.next();
  //       })
  //   }

  stringToByte(str) {
    var bytes = new Array();
    var len, c;
    len = str.length;
    for (var i = 0; i < len; i++) {
      c = str.charCodeAt(i);
      if (c >= 0x010000 && c <= 0x10ffff) {
        bytes.push(((c >> 18) & 0x07) | 0xf0);
        bytes.push(((c >> 12) & 0x3f) | 0x80);
        bytes.push(((c >> 6) & 0x3f) | 0x80);
        bytes.push((c & 0x3f) | 0x80);
      } else if (c >= 0x000800 && c <= 0x00ffff) {
        bytes.push(((c >> 12) & 0x0f) | 0xe0);
        bytes.push(((c >> 6) & 0x3f) | 0x80);
        bytes.push((c & 0x3f) | 0x80);
      } else if (c >= 0x000080 && c <= 0x0007ff) {
        bytes.push(((c >> 6) & 0x1f) | 0xc0);
        bytes.push((c & 0x3f) | 0x80);
      } else {
        bytes.push(c & 0xff);
      }
    }
    return bytes;
  }

  //   byteToString(arr) {
  //       if (typeof arr === 'string') {
  //           return arr;
  //       }
  //       var str = '',
  //           _arr = arr;
  //       for (var i = 0; i < _arr.length; i++) {
  //           var one = _arr[i].toString(2),
  //               v = one.match(/^1+?(?=0)/);
  //           if (v && one.length == 8) {
  //               var bytesLength = v[0].length;
  //               var store = _arr[i].toString(2).slice(7 - bytesLength);
  //               for (var st = 1; st < bytesLength; st++) {
  //                   store += _arr[st + i].toString(2).slice(2);
  //               }
  //               str += String.fromCharCode(parseInt(store, 2));
  //               i += bytesLength - 1;
  //           } else {
  //               str += String.fromCharCode(_arr[i]);
  //           }
  //       }
  //       return str;
  //   }

  getEventLog(eventType: string = 'AC1', eventMsg: string): AA0206Req {
    return {
      isLogin: this.getUserID() ? 'true' : 'false',
      agent: navigator.userAgent,
      eventType: eventType,
      eventMsg: eventMsg,
    };
  }

  //   aesEncrypt(original: string, key: number[], iv: number[]): string {
  //       let textBytes = aesjs.utils.utf8.toBytes(original);
  //       // 建立 CBC 串鏈
  //       let aesCbc = new aesjs.ModeOfOperation.cbc(key, iv);
  //       let encryptedBytes = aesCbc.encrypt(textBytes);
  //       // 加密過後的資料是二進位資料，若要輸出可轉為十六進位格式 (或是Base64格式)
  //       let encryptedHex = aesjs.utils.hex.fromBytes(encryptedBytes);
  //       return encryptedHex;
  //   }

  //   aesDeCrypt(encryptedHex: string, key: number[], iv: number[]): string {
  //       // 將十六進位的資料轉回二進位
  //       let encryptedBytes = aesjs.utils.hex.toBytes(encryptedHex);
  //       // 由於舊的 CBC 串鏈會儲存一些內部的狀態，
  //       // 所以解密時要重新建立一個新的 CBC 串鍊
  //       let aesCbc = new aesjs.ModeOfOperation.cbc(key, iv);
  //       let decryptedBytes = aesCbc.decrypt(encryptedBytes);
  //       // 將二進位資料轉換回文字
  //       let decryptedText = aesjs.utils.utf8.fromBytes(decryptedBytes);
  //       return decryptedText;
  //   }

  setHyperLinkAuth(result: { funCode: string; canExecute: boolean }[]) {
    sessionStorage.setItem('HyperLinkAuth', JSON.stringify(result));
  }

  getHyperLinkAuth(): { funCode: string; canExecute: boolean }[] {
    return JSON.parse(sessionStorage.getItem('HyperLinkAuth') ?? '');
  }

  getTimeRange() {
    return Object.keys(TimeRange).map((key) => {
      return {
        label: key,
        value: TimeRange[key],
      };
    });
  }

  padLeft(str, lenght) {
    // if (isNullOrUndefined(str) || str == '') {
    //     return '';
    // }
    // else {
    if (isNumber(str)) {
      str = str.toString();
    }
    if (str.length >= lenght) {
      return str;
    } else {
      return this.padLeft('0' + str, lenght);
    }
    // }
  }

  //   stringArrayToObject(strArr: string[]) {
  //       return strArr.reduce(function (result, item) {
  //           var key = item; //first property: a, b, c
  //           result[key] = undefined;
  //           return result;
  //       }, {});
  //   }

  async getDict(codes: string[], params?: object): Promise<object> {
    return await this.translate.get(codes, params).toPromise();
  }

  getAcConfEdition() {
    return sessionStorage.getItem('AcConf')
      ? JSON.parse(sessionStorage.getItem('AcConf')!)['edition']
      : '';
  }

  getAcConfExpiryDate() {
    return sessionStorage.getItem('AcConf')
      ? JSON.parse(sessionStorage.getItem('AcConf')!)['expiryDate']
      : '';
  }

  setEnvListData(item: LdapEnvItem[]) {
    sessionStorage.setItem('envList', JSON.stringify(item));
  }

  getEnvListData(): [] {
    return sessionStorage.getItem('envList')
      ? JSON.parse(sessionStorage.getItem('envList')!)
      : [];
  }

  //點選api，更新標記
  setExpiredTime() {
    this.expiredTimeEvt.next(new Date());
  }

  getExpiredTime() {
    return this.expiredTimeEvt.asObservable();
  }

  setClearExpiredTimeout() {
    this.clearExpiredTimeout = new Subject();
    this.clearExpiredTimeout.next(true);
  }

  procClearExpiredTimeout() {
    return this.clearExpiredTimeout.asObservable();
  }

  composerUrl(composer: {
    port: string;
    path: string;
    apiUid: string;
    authCode: string;
    moduleName: string;
    apiKey: string;
  }) {
    // const url = `${location.protocol}//${location.hostname}:${composer.port}${composer.path}?ac=${composer.authCode}&moduleName=${composer.moduleName}&apiKey=${composer.apiKey}`;
    const url = `${location.protocol}//${location.hostname}:${location.port}/website/composer/${composer.apiUid}?ac=${composer.authCode}&moduleName=${composer.moduleName}&apiKey=${composer.apiKey}`;
    return url;
  }

  /** 千分位補點 */
  numberComma(num) {
    let comma = /\B(?<!\.\d*)(?=(\d{3})+(?!\d))/g;
    return num.toString().replace(comma, ',');
  }

  /**
   *
   * @param oriString 原字串
   * @param policy 0:無遮罩 1:留前後 2:留前 3:留後 => 其餘遮罩
   * @param charNum 遮罩字元數
   * @param mask 取代後的字元
   * @returns
   */
  maskStringByPolicy(
    oriString: string = '',
    policy: string = '1',
    charNum: number = 1,
    mask: string = '*'
  ) {
    let replacement = '';
    // console.log('oriString', oriString)
    // console.log('policy', policy)
    // console.log('charNum', charNum)
    // console.log('mask', mask)

    switch (policy) {
      case '1': //前後
        if (oriString.length <= 2 * charNum) {
          replacement = mask.repeat(oriString.length);
        } else {
          let start = oriString.substring(0, charNum);
          let end = oriString.substring(oriString.length - charNum);
          let middle = mask.repeat(oriString.length - 2 * charNum);
          replacement = start + middle + end;
        }
        break;
      case '2': //前
        if (oriString.length <= charNum) {
          replacement = mask.repeat(oriString.length);
        } else {
          let start = oriString.substring(0, charNum);
          let end = mask.repeat(oriString.length - charNum);
          replacement = start + end;
        }
        break;
      case '3': //後
        if (oriString.length <= charNum) {
          replacement = mask.repeat(oriString.length);
        } else {
          let start = mask.repeat(oriString.length - charNum);
          let end = oriString.substring(oriString.length - charNum);
          replacement = start + end;
        }
        break;
      default:
        break;
    }
    return replacement;
  }

  isValidJSON(str: string) {
    try {
      JSON.parse(str);
      return true;
    } catch (e) {
      return false;
    }
  }

  // /**
  //  *
  //  * @param oriString 原字串
  //  * @param policy 0:無遮罩 1:遮前後 2:遮前 3:遮後 => 其餘不遮
  //  * @param charNum 遮罩字元數
  //  * @param mask 取代後的字元
  //  * @returns
  //  */
  // bodyMaskStringByPolicy(oriStr: string = '', policy: string = '1', charNum: number = 3, mask: string = '*') {
  //   let replacement = '';
  //   const bodyArr = oriStr.split(',')
  //   const bodyStr = bodyArr.map(body=>{
  //     return body == '' ?'': `"${body}":"example"`
  //   }).join(',');

  //   // bodyArr.map(data=> {
  //   //   bodyStr.
  //   // })

  //   return bodyStr;

  //   // switch (policy) {
  //   //   case '1': //前後
  //   //     if (oriString.length <= 2 * charNum) {
  //   //       replacement = mask.repeat(oriString.length)
  //   //     } else {
  //   //       let start = mask.repeat(charNum)
  //   //       let end = mask.repeat(charNum)
  //   //       let middle = oriString.substring(charNum,oriString.length - charNum)
  //   //       replacement = start + middle + end;
  //   //     }
  //   //     break;
  //   //   case '2': //前
  //   //     if (oriString.length <= charNum) {
  //   //       replacement = mask.repeat(oriString.length)
  //   //     } else {
  //   //       let start = mask.repeat(charNum)
  //   //       let end = oriString.substring(charNum);
  //   //       replacement = start + end;
  //   //     }
  //   //     break;
  //   //   case '3': //後
  //   //   if (oriString.length <= charNum) {
  //   //     replacement = mask.repeat(oriString.length)
  //   //   } else {
  //   //     let start = mask.repeat(oriString.length - charNum)
  //   //     let end = oriString.substring(oriString.length - charNum)
  //   //     replacement = start + end;
  //   //   }
  //   //     break;
  //   //   default:

  //   //     break;
  //   // }
  //   return replacement;
  // }

  // replaceMsskByKeyword(bodyStr,key){
  //   let arr = bodyStr.split(key);
  //   let ss = arr.map(row=>{

  //   })
  // }

  /**
   * 20220113新增
   * 把menu(由Tsmp_func取回)的資料跟前端routes source(layout.routing.ts)的id做比對
   * 若menu.func_code無法對應到routes的id，則移除
   */
  validateMenusNFuncCode(menu: any) {
    let layoutRouteData = new Array(); // 用來記錄有在layout.routing.ts內註冊的頁面id
    const regMenuData = routes[0]?.children;
    if (regMenuData) {
      for (let i = 0; i < regMenuData.length; i++) {
        if (regMenuData[i].data && regMenuData[i].data!['id']) {
          layoutRouteData.push(regMenuData[i].data!['id'].toUpperCase());
        }
      }
    }

    menu.forEach((item: Menu) => {
      item.subs = item.subs?.filter((subitem) =>
        layoutRouteData.find((id) => {
          return (
            subitem.name.startsWith('ZA') ||
            subitem.name.startsWith('AC09') ||
            id === subitem.name
          );
        })
      );
    });
    // console.log('menu:',menu)

    menu = menu.filter((item: Menu) => {
      return item.subs && item.subs.length > 0 ? true : false;
    });
    // console.log('menu:',menu)
    return menu;
  }

  validateUrl(url: string): boolean {
    try {
      const parsedUrl = new URL(url);
      // 僅允許 http 和 https 協議
      return ['http:', 'https:'].includes(parsedUrl.protocol);
    } catch {
      return false; // 無效的 URL
    }
  }

  generate_uuid() {
    var d = Date.now();
    if (
      typeof performance !== 'undefined' &&
      typeof performance.now === 'function'
    ) {
      d += performance.now(); //use high-precision timer if available
    }
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(
      /[xy]/g,
      function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : (r & 0x3) | 0x8).toString(16);
      }
    );
  }

  dgrProtocol_match(testString: string = '') {
    // /^(dgr)((\+[-a-zA-Z]+)+)##(.+)/
    // ^dgr(\\+[-a-zA-Z]+)+##(.+)
    // 'dgr+ai-gateway+mtls+webhook+chatgpt##https://chad.test/1'
    const match = testString.match(/^(dgr)((\+[-a-zA-Z]+)+)##(.+)/);

    if (match) {
      const base = match[1]; // dgr
      const plusParts = match[2].match(/\+[-a-zA-Z]+/g) || []; // 所有 +xxx
      const url = match[4]; // ## 後面的內容
      return [base, ...plusParts, url];
    }
    return [];
  }

  //判斷是否mtls來增加或移除url字樣
  toggleMtlsFlag(input: string, ismtls: boolean): string {
    if (input == '') return '';
    const dgrPrefixRegex = /^dgr\+(.+?)##(.*)$/;

    const match = input.match(dgrPrefixRegex);

    if (match) {
      // 符合 dgr+...## 的格式
      const prefix = match[1]; // 例如 webhook、webhook+mtls、mtls
      const rest = match[2]; // 例如 prompt/xxx

      const services = prefix.split('+').filter(Boolean);
      const hasMtls = services.includes('mtls');

      if (ismtls) {
        // 增加 +mtls（避免重複加）
        if (!hasMtls) services.push('mtls');
      } else {
        // 移除 +mtls
        const filtered = services.filter((s) => s !== 'mtls');
        if (filtered.length === 0) {
          // 僅剩 mtls，代表原本是 dgr+mtls##xxx ➜ 直接移除整個前綴
          return rest;
        }
        return `dgr+${filtered.join('+')}##${rest}`;
      }

      return `dgr+${services.join('+')}##${rest}`;
    } else {
      // 非 dgr+## 開頭的字串
      return ismtls ? `dgr+mtls##${input}` : input;
    }
  }

  checkSrcUrlisAiGateway(_srcUrl: string): boolean {
    let isAiGateway: boolean = false;
    if (_srcUrl.includes('b64.')) {
      if (_srcUrl.substring(0, 3) == 'b64') {
        let srcUrlArr = _srcUrl.split('.');
        srcUrlArr.shift();

        let srcUrlArrEdit: { percent: string; url: string }[] = [];
        for (let i = 0; i < srcUrlArr.length; i++) {
          if (i % 2 == 0) {
            srcUrlArrEdit.push({
              percent: srcUrlArr[i],
              url: base64.Base64.decode(srcUrlArr[i + 1]),
            });
          }
        }

        isAiGateway = srcUrlArrEdit.some((item) => {
          const dgrProtocol_match = this.dgrProtocol_match(item.url);
          return dgrProtocol_match.some((item) => item.includes('ai-gateway'));
        });
      }
    } else {
      const dgrProtocol_match = this.dgrProtocol_match(_srcUrl);
      isAiGateway = dgrProtocol_match.some((item) =>
        item.includes('ai-gateway')
      );
    }
    return isAiGateway;
  }

  checkSrcUrlisWebhook(_srcUrl: string): boolean {
    let isWebhook: boolean = false;
    if (!_srcUrl.includes('b64.')) {
      const dgrProtocol_match = this.dgrProtocol_match(_srcUrl);

      if (dgrProtocol_match.length > 0) {
        // console.log(dgrProtocol_match)
        isWebhook = dgrProtocol_match.some((item) => item.includes('webhook'));
      }
    }
    return isWebhook;
  }
}

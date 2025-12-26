import { HttpClient } from '@angular/common/http';
import { environment } from 'src/environments/environment';
import {
  DPB0039Req,
  RespDPB0039,
  ReqDPB0039,
} from '../../models/api/UserService/dpb0039.interface';
import {
  AA0019Req,
  ResAA0019,
  ReqAA0019,
} from '../../models/api/UserService/aa0019.interface';
import {
  ResAA0012,
  ReqAA0012,
} from '../../models/api/UserService/aa0012.interface';
import {
  ResAA0009,
  ReqAA0009,
} from '../../models/api/UserService/aa0009.interface';
import {
  AA0006Req,
  ResAA0006,
  ReqAA0006,
  ResAA0006Before,
} from '../../models/api/UserService/aa0006.interface';
import {
  AA0005Req,
  ResAA0005,
  ReqAA0005,
} from '../../models/api/UserService/aa0005.interface';
import {
  AA0004Req,
  ResAA0004,
  ReqAA0004,
  ResAA0004Before,
} from '../../models/api/UserService/aa0004.interface';
import {
  AA0003Req,
  ResAA0003,
  ReqAA0003,
} from '../../models/api/UserService/aa0003.interface';
import {
  ResAA0002,
  ReqAA0002,
} from '../../models/api/UserService/aa0002.interface';
import { ToolService } from './tool.service';
import { ApiBaseService } from './api-base.service';
import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import { TxID } from 'src/app/models/common.enum';
import {
  AA0001Req,
  ResAA0001,
  ReqAA0001,
  ResAA0001Before,
} from 'src/app/models/api/UserService/aa0001.interface';
import {
  AA0549Req,
  ReqAA0549,
  ResAA0549,
  ResAA0549Before,
} from 'src/app/models/api/UserService/aa0549.interface';
import {
  AA0550Req,
  ReqAA0550,
  ResAA0550,
  ResAA0550Before,
} from 'src/app/models/api/UserService/aa0550.interface';
import { AA0551Req, ReqAA0551, ResAA0551, ResAA0551Before } from 'src/app/models/api/UserService/aa0551.interface';
import { AA0552Req, AA0552Resp, ReqAA0552, ResAA0552, ResAA0552Before, ResAA0552RespBefore } from 'src/app/models/api/UserService/aa0552.interface';
import { ResAA0553 } from 'src/app/models/api/UserService/aa0553.interface';

@Injectable()
export class UserService {
  public get npBasePath(): string {
    return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
  }

  constructor(private api: ApiBaseService, private toolService: ToolService, private httpClient: HttpClient) {}

  /**
   * AA0001: 新增TUser
   * 在TSMP Admin中新增TSMP後台管理使用者，簡稱TUser。
   * 新增成功時返回userID。
   * @param body ReqAA0001
   */
  addTUser(ReqBody: AA0001Req): Observable<ResAA0001> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTUser),
      ReqBody: ReqBody,
    } as ReqAA0001;
    const path = `${this.npBasePath}/AA0001`;
    return this.api.npPost<ResAA0001>(path, body);
  }

  addTUser_before(): Observable<ResAA0001Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.addTUser),
      ReqBody: {},
    } as ReqAA0001;
    const path = `${this.npBasePath}/AA0001?before`;
    return this.api.npPost<ResAA0001Before>(path, body);
  }

  /**
   * v3版
   * AA0002: 查詢TUserList
   * 在TSMP Admin中查詢TSMP後台管理使用者清單。預設查詢所有TUser。
   * @param body ReqAA0002
   */
  queryUserDataByLoginUser(): Observable<ResAA0002> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryUserDataByLoginUser),
      ReqBody: {},
    } as ReqAA0002;
    const path = `${this.npBasePath}/AA0002`;
    return this.api.npPost<ResAA0002>(path, body);
  }

  /**
   * AA0003: 查詢TUserDetail
   * 在TSMP Admin中查詢TSMP後台管理使用者資料。需同時有ID與Name才能查詢Tuser Detail。
   * @param body ReqAA0003
   */
  queryTUserDetail(ReqBody: AA0003Req): Observable<ResAA0003> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTUserDetail),
      ReqBody: ReqBody,
    } as ReqAA0003;
    const path = `${this.npBasePath}/AA0003`;
    return this.api.npPost<ResAA0003>(path, body);
  }

  /**
   * AA0004: 更新TUser
   * 在TSMP Admin中更新TSMP後台管理使用者資料。需同時有ID與Name才能更新TUser。
   * 此API只有Administrator可以使用，一般TUser不行。
   * @param body ReqAA0004
   */
  updateTUserState(ReqBody: AA0004Req): Observable<ResAA0004> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTUserState),
      ReqBody: ReqBody,
    } as ReqAA0004;
    const path = `${this.npBasePath}/AA0004`;
    return this.api.npPost<ResAA0004>(path, body);
  }

  updateTUserState_before(): Observable<ResAA0004Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTUserState),
      ReqBody: {},
    } as ReqAA0004;
    const path = `${this.npBasePath}/AA0004?before`;
    return this.api.npPost<ResAA0004Before>(path, body);
  }

  /**
   * AA0005: 刪除TUser
   * 在TSMP Admin中刪除TSMP後台管理使用者。需同時有ID與Name才能刪除TUser。
   * 此API只有Administrator可以使用，一般TUser不行。
   * @param body ReqAA0005
   */
  deleteTUser(ReqBody: AA0005Req): Observable<ResAA0005> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.deleteTUser),
      ReqBody: ReqBody,
    } as ReqAA0005;
    const path = `${this.npBasePath}/AA0005`;
    return this.api.npPost<ResAA0005>(path, body);
  }

  /**
   * AA0006: 更新TUserData
   * 在TSMP Admin中更新TSMP後台管理使用者資料。需同時有ID與Name，舊密碼相符時才能更新TUser密碼。
   * @param body ReqAA0006
   */
  updateTUserData(ReqBody: AA0006Req): Observable<ResAA0006> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTUserData),
      ReqBody: this.toolService.deleteProperties(ReqBody),
    } as ReqAA0006;
    const path = `${this.npBasePath}/AA0006`;
    // return Observable.create(obser => {
    //     let obj = {
    //         ResHeader: {
    //             "txSN": "1181023112022OffLZP",
    //             "txDate": "20181023T112022+0800",
    //             "txID": "AA0006",
    //             "rtnCode": "0000",
    //             "rtnMsg": "success"
    //         } as ResHeader
    //     } as ResAA0006;
    //     obser.next(obj);
    // })
    return this.api.npPost<ResAA0006>(path, body);
  }

  updateTUserData_before(): Observable<ResAA0006Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateTUserData),
      ReqBody: {},
    } as ReqAA0006;
    const path = `${this.npBasePath}/AA0006?before`;
    return this.api.npPost<ResAA0006Before>(path, body);
  }

  /**
   * AA0009: 登出TUser
   * 在TSMP Admin中將TUser登出，即讓Token失效。預計做法: 執行登出時，系統在LOGOFF_DATE記錄時間，
   * 則接下來所有進來請求的AccessToken或RefreshToke的timestamp皆要大於此時間才行，否則回覆"0001"(請重新登入)。
   * @param body
   */
  logoutTUser(): Observable<ResAA0009> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.logoutTUser),
      ReqBody: {},
    } as ReqAA0009;
    const path = `${this.npBasePath}/AA0009`;
    return this.api.npPost<ResAA0009>(path, body);
  }

  /**
   * v3版
   * queryFuncByLoginUser: 取得登入者的功能清單
   * @param ReqBody
   */
  queryFuncByLoginUser(): Observable<ResAA0012> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryFuncByLoginUser),
      ReqBody: {},
    } as ReqAA0012;
    const path = `${this.npBasePath}/AA0012`;
    return this.api.npPost<ResAA0012>(path, body);
  }

  /**
   * 忽略查無資料
   * AA0019: 在TSMP中查詢後台管理使用者清單。
   * @param ReqBody
   */
  queryTUserList_v3_ignore1298(ReqBody: AA0019Req): Observable<ResAA0019> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTUserList_v3),
      ReqBody: ReqBody,
    } as ReqAA0019;
    const path = `${this.npBasePath}/AA0019`;
    return this.api.excuteNpPost_ignore1298<ResAA0019>(path, body);
  }

  /**
   * AA0019: 在TSMP中查詢後台管理使用者清單。
   * @param ReqBody
   */
  queryTUserList_v3(ReqBody: AA0019Req): Observable<ResAA0019> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTUserList_v3),
      ReqBody: ReqBody,
    } as ReqAA0019;
    const path = `${this.npBasePath}/AA0019`;
    return this.api.npPost<ResAA0019>(path, body);
  }

  /**
   * DPB0039: Tsmp管理者Like查詢
   */
  queryTsmpUserLikeList(ReqBody: DPB0039Req): Observable<RespDPB0039> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.queryTsmpUserLikeList),
      ReqBody: ReqBody,
    } as ReqDPB0039;
    const path = `${this.npBasePath}/DPB0039`;
    return this.api.npPost<RespDPB0039>(path, body);
  }

  //
  updateNewMima(ReqBody: AA0549Req): Observable<ResAA0549> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateNewMima),
      ReqBody: ReqBody,
    } as ReqAA0549;
    const path = `${this.npBasePath}/AA0549`;
    return this.api.npPost<ResAA0549>(path, body);
  }

  updateNewMima_before(): Observable<ResAA0549Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.updateNewMima),
      ReqBody: {},
    } as ReqAA0549;
    const path = `${this.npBasePath}/AA0549?before`;
    return this.api.excuteNpPost_ignore1298<ResAA0549Before>(path, body);
  }

  // 取得otp驗證 (沒有token故不需走攔截器流程)
  sendOtp(ReqBody: AA0550Req): Observable<ResAA0550> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.sendOtp),
      ReqBody: ReqBody,
    } as ReqAA0550;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0550`;
    return this.httpClient.post<ResAA0550>(path, body);
  }

  // 取得sendOtp欄位檢核 (沒有token故不需走攔截器流程)
  sendOtp_before(): Observable<ResAA0550Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.sendOtp),
      ReqBody: {},
    } as ReqAA0550;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0550?before`;
    return this.httpClient.post<ResAA0550Before>(path, body);
  }

  validationOtp_before(): Observable<ResAA0551Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.sendOtp),
      ReqBody: {},
    } as ReqAA0551;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0551?before`;
    return this.httpClient.post<ResAA0551Before>(path, body);
  }

  validationOtp(ReqBody: AA0551Req): Observable<ResAA0551> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.sendOtp),
      ReqBody: ReqBody,
    } as ReqAA0551;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0551`;
    return this.httpClient.post<ResAA0551>(path, body);
  }

  changeMima(ReqBody: AA0552Req): Observable<ResAA0552> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.changeMima),
      ReqBody: ReqBody,
    } as ReqAA0552;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0552`;
    return this.httpClient.post<ResAA0552>(path, body);
  }

  changeMima_before(): Observable<ResAA0552Before> {
    let body = {
      ReqHeader: this.api.getReqHeader(TxID.changeMima),
      ReqBody: {},
    } as ReqAA0552;
    const path = `${environment.apiUrl}/${this.npBasePath}/AA0552?before`;
    return this.httpClient.post<ResAA0552Before>(path, body);
  }

  getMimaStrengthDesc(){
    const path = `${this.npBasePath}/AA0553`;
    return this.api.excuteDpGet<ResAA0553>(path, TxID.getMimaStrengthDesc);
  }
}

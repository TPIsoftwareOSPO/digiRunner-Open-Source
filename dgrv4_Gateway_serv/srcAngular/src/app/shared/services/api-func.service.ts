import { environment } from 'src/environments/environment';
import { AA0104Req, ResAA0104, ReqAA0104 } from '../../models/api/FuncService/aa0104.interface';
import { AA0103Req, ResAA0103, ReqAA0103 } from '../../models/api/FuncService/aa0103.interface';
import { AA0102Req, ResAA0102, ReqAA0102 } from '../../models/api/FuncService/aa0102.interface';
import { ResAA0101, ReqAA0101 } from '../../models/api/FuncService/aa0101.interface';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { Observable } from 'rxjs';
import { TxID } from 'src/app/models/common.enum';
import { AA0106Req, ReqAA0106, ResAA0106, RespAA0106Before } from 'src/app/models/api/FuncService/aa0106.interface';
import { AA0107Req, ReqAA0107, ResAA0107 } from 'src/app/models/api/FuncService/aa0107.interface';
import { AA0108Req, ReqAA0108, ResAA0108 } from 'src/app/models/api/FuncService/aa0108.interface';


@Injectable()
export class FuncService {

    public get npBasePath(): string {
        return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) { }

    /**
     * v3
     * AA0101: 查詢TFuncList
     * 查詢TSMP Admin的功能清單。TSMP Admin的功能簡稱TFunc。
     * @param body ReqAA0101
     */
    queryAllFunc(): Observable<ResAA0101> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAllFunc),
            ReqBody: {}
        } as ReqAA0101
        const path = `${this.npBasePath}/AA0101`;
        return this.api.npPost<ResAA0101>(path, body);
    }

    /**
     * AA0102: 更新TFunc
     * 更新TSMP Admin的功能名稱與說明。
     * @param body ReqAA0102
     */
    updateTFunc(ReqBody: AA0102Req): Observable<ResAA0102> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateFunction),
            ReqBody: ReqBody
        } as ReqAA0102
        const path = `${this.npBasePath}/AA0102`;
        return this.api.npPost<ResAA0102>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0103: 查詢TFuncList
     */
    queryTFuncList_v3_ignore1298(ReqBody: AA0103Req): Observable<ResAA0103> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTFuncList_v3),
            ReqBody: ReqBody
        } as ReqAA0103
        const path = `${this.npBasePath}/AA0103`;
        return this.api.excuteNpPost_ignore1298<ResAA0103>(path, body);
    }

    /**
     * AA0103: 查詢TFuncList
     */
    queryTFuncList_v3(ReqBody: AA0103Req): Observable<ResAA0103> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTFuncList_v3),
            ReqBody: ReqBody
        } as ReqAA0103
        const path = `${this.npBasePath}/AA0103`;
        return this.api.npPost<ResAA0103>(path, body);
    }

    /**
     * AA0104: 查詢單一關聯角色
     */
    queryTFuncRoleList(ReqBody: AA0104Req): Observable<ResAA0104> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTFuncRoleList),
            ReqBody: ReqBody
        } as ReqAA0104
        const path = `${this.npBasePath}/AA0104`;
        return this.api.npPost<ResAA0104>(path, body);
    }

    queryTFuncRoleList_ignore1298(ReqBody: AA0104Req): Observable<ResAA0104> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.queryTFuncRoleList),
          ReqBody: ReqBody
      } as ReqAA0104
      const path = `${this.npBasePath}/AA0104`;
      return this.api.excuteNpPost_ignore1298<ResAA0104>(path, body);
  }

    /**AA0106 新增報表功能 */
    addReport(ReqBody: AA0106Req): Observable<ResAA0106> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.addReport),
          ReqBody: ReqBody
      } as ReqAA0106
      const path = `${this.npBasePath}/AA0106`;
      return this.api.npPost<ResAA0106>(path, body);
    }

    /**AA0106 新增報表功能 */
    addReport_before(): Observable<RespAA0106Before> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.addReport),
          ReqBody: {}
      } as ReqAA0106
      const path = `${this.npBasePath}/AA0106?before`;
      return this.api.npPost<RespAA0106Before>(path, body);
    }

    /** 刪除報表功能 */
    deleteReport(ReqBody: AA0107Req): Observable<ResAA0107> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.deleteReport),
          ReqBody: ReqBody
      } as ReqAA0107
      const path = `${this.npBasePath}/AA0107`;
      return this.api.npPost<ResAA0107>(path, body);
    }

    /** 查詢客製主選單 */
    queryCusMasterFunc(): Observable<ResAA0108> {
      let body = {
          ReqHeader: this.api.getReqHeader(TxID.queryCusMasterFunc),
          ReqBody: {}
      } as ReqAA0108
      const path = `${this.npBasePath}/AA0108`;
      return this.api.npPost<ResAA0108>(path, body);
    }

}

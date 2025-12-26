import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import { AA0705Req, ReqAA0705, ResAA0705 } from 'src/app/models/api/AlertService/aa0705.interface';
import { AA0706Req, ReqAA0706, ResAA0706 } from 'src/app/models/api/AlertService/aa0706.interface';
import { AA0701Req, ResAA0701, ReqAA0701, ResAA0701Before } from 'src/app/models/api/AlertService/aa0701.interface';
import { AA0703Req, ResAA0703, ReqAA0703 } from 'src/app/models/api/AlertService/aa0703.interface';
import { AA0704Req, ResAA0704, ReqAA0704, ResAA0704Before } from 'src/app/models/api/AlertService/aa0704.interface';

@Injectable()
export class ApiAlterService {

    public get npBasePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) { }

    /**
     * before
     * AA0701: 新增告警設定
     * @param ReqBody
     */
    addAlarmSettings_before(): Observable<ResAA0701Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addAlarmSettings),
            ReqBody: {}
        } as ReqAA0701
        const path = `${this.npBasePath}/AA0701?before`;
        return this.api.npPost<ResAA0701Before>(path, body);
    }

    /**
     * AA0701: 新增告警設定
     * @param ReqBody
     */
    addAlarmSettings(ReqBody: AA0701Req): Observable<ResAA0701> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addAlarmSettings),
            ReqBody: ReqBody
        } as ReqAA0701
        const path = `${this.npBasePath}/AA0701`;
        return this.api.npPost<ResAA0701>(path, body);
    }

    /**
     * AA0703: 查詢告警設定明細
     * @param ReqBody
     */
    queryAlertSettingDetail(ReqBody: AA0703Req): Observable<ResAA0703> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAlertSettingDetail),
            ReqBody: ReqBody
        } as ReqAA0703
        const path = `${this.npBasePath}/AA0703`;
        return this.api.npPost<ResAA0703>(path, body);
    }

    /**
     * before
     * AA0704: 更新告警設定
     * @param ReqBody
     */
    updateAlertSetting_before(): Observable<ResAA0704Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateAlertSetting),
            ReqBody: {}
        } as ReqAA0704
        const path = `${this.npBasePath}/AA0704?before`;
        return this.api.npPost<ResAA0704Before>(path, body);
    }

    /**
     * AA0704: 更新告警設定
     * @param ReqBody
     */
    updateAlertSetting(ReqBody: AA0704Req): Observable<ResAA0704> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateAlertSetting),
            ReqBody: ReqBody
        } as ReqAA0704
        const path = `${this.npBasePath}/AA0704`;
        return this.api.npPost<ResAA0704>(path, body);
    }

    /**
     * AA0705: 刪除告警設定
     */
    deleteAlertSetting(ReqBody: AA0705Req): Observable<ResAA0705> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteAlert),
            ReqBody: ReqBody
        } as ReqAA0705
        const path = `${this.npBasePath}/AA0705`;
        return this.api.npPost<ResAA0705>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0706: 查詢告警設定
     */
    queryAlarmSettings_ignore1298(ReqBody: AA0706Req): Observable<ResAA0706> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAlarmSettings),
            ReqBody: ReqBody
        } as ReqAA0706
        const path = `${this.npBasePath}/AA0706`;
        return this.api.excuteNpPost_ignore1298<ResAA0706>(path, body);
    }

    /**
     * AA0706: 查詢告警設定
     */
    queryAlarmSettings(ReqBody: AA0706Req): Observable<ResAA0706> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAlarmSettings),
            ReqBody: ReqBody
        } as ReqAA0706
        const path = `${this.npBasePath}/AA0706`;
        return this.api.npPost<ResAA0706>(path, body);
    }

}

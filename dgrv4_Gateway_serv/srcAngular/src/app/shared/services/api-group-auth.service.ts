import { environment } from 'src/environments/environment';
import { Injectable } from '@angular/core';
import { ApiBaseService } from './api-base.service';
import { Observable } from 'rxjs';
import { TxID } from 'src/app/models/common.enum';
import { Req_1102, ResAA1102, ReqAA1102 } from 'src/app/models/api/GroupAuthService/aa1102.interface';
import { ResAA1108, ReqAA1108, AA1108Req, ResAA1108Before } from 'src/app/models/api/GroupAuthService/aa1108.interface';
import { ResAA1109, ReqAA1109, AA1109Req } from 'src/app/models/api/GroupAuthService/aa1109.interface';
import { ResAA1110, ReqAA1110, AA1110Req } from 'src/app/models/api/GroupAuthService/aa1110.interface';
import { ResAA1106, ReqAA1106, AA1106Req, ResAA1106Before } from 'src/app/models/api/GroupAuthService/aa1106.interface';
import { AA1115Req, ReqAA1115, ResAA1115 } from 'src/app/models/api/GroupAuthService/aa1115.interface';

@Injectable({
    providedIn: 'root'
})
export class GroupAuthService {

    public get npBasePath(): string {
        return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService,
    ) { }

    /**
     * AA1102: 查詢Security Level List
     * 在TSMP Admin中查詢Security Level，簡稱queryTSecurityLV。
     * 查詢成功時返回安全等級List。
     * @param Req_1102
     */
    queryTSecurityLV(req_1102: Req_1102): Observable<ResAA1102> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTSecurityLV),
            ReqBody: req_1102
        } as ReqAA1102
        const path = `${this.npBasePath}/AA1102`;
        return this.api.npPost<ResAA1102>(path, body);
    }

    /**
     * before
     * AA1106: 新增Group Authority
     * 在TSMP Admin中新增Group Authority，簡稱addTGroupAuthority。
     * 新增成功時返回GROUP_AUTHORITIE_NAME。
     */
    addTGroupAuthority_before(): Observable<ResAA1106Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addTGroupAuthority),
            ReqBody: {}
        } as ReqAA1106
        const path = `${this.npBasePath}/AA1106?before`;
        return this.api.npPost<ResAA1106Before>(path, body);
    }

    /**
     * AA1106: 新增Group Authority
     * 在TSMP Admin中新增Group Authority，簡稱addTGroupAuthority。
     * 新增成功時返回GROUP_AUTHORITIE_NAME。
     */
    addTGroupAuthority(ReqBody: AA1106Req): Observable<ResAA1106> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addTGroupAuthority),
            ReqBody: ReqBody
        } as ReqAA1106
        const path = `${this.npBasePath}/AA1106`;
        return this.api.npPost<ResAA1106>(path, body);
    }

    /**
     * before
     * AA1108: 更新Group Authority
     * 在TSMP Admin中更新Group Authority，簡稱updateTGroupAuthority。
     * 更新成功時返回核身驗證發法的GROUP AUTHORITIE ID。
     */
    updateTGroupAuthority_before(): Observable<ResAA1108Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTGroupAuthority),
            ReqBody: {}
        } as ReqAA1108
        const path = `${this.npBasePath}/AA1108?before`;
        return this.api.npPost<ResAA1108Before>(path, body);
    }

    /**
     * AA1108: 更新Group Authority
     * 在TSMP Admin中更新Group Authority，簡稱updateTGroupAuthority。
     * 更新成功時返回核身驗證發法的GROUP AUTHORITIE ID。
     */
    updateTGroupAuthority(ReqBody: AA1108Req): Observable<ResAA1108> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTGroupAuthority),
            ReqBody: ReqBody
        } as ReqAA1108
        const path = `${this.npBasePath}/AA1108`;
        return this.api.npPost<ResAA1108>(path, body);
    }

    /**
     * AA1109: 查詢Group Authority Detail
     * 在TSMP Admin中獲取Group Authority的Detail，簡稱queryTGroupAuthorityDetail。
     * 成功時返回核身驗證發法的Detail。
     */
    queryTGroupAuthorityDetail(ReqBody: AA1109Req): Observable<ResAA1109> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTGroupAuthorityDetail),
            ReqBody: ReqBody
        } as ReqAA1109
        const path = `${this.npBasePath}/AA1109`;
        return this.api.npPost<ResAA1109>(path, body);
    }

    /**
     * AA1110: 刪除Group Authority
     * 在TSMP Admin中刪除Group Authority，簡稱deleteTGroupAuthority。
     */
    deleteTGroupAuthority(ReqBody: AA1110Req): Observable<ResAA1110> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteTGroupAuthority),
            ReqBody: ReqBody
        } as ReqAA1110
        const path = `${this.npBasePath}/AA1110`;
        return this.api.npPost<ResAA1110>(path, body);
    }

    /**
     * 忽略查無資料
     * AA1115: 查詢查詢核身方式的List(原AA1107)
     *
     */
    queryScopeAuthorities_ignore1298(ReqBody: AA1115Req): Observable<ResAA1115> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryScopeAuthorities),
            ReqBody: ReqBody
        } as ReqAA1115
        const path = `${this.npBasePath}/AA1115`;
        return this.api.excuteNpPost_ignore1298<ResAA1115>(path, body);
    }

    /**
     * AA1115: 查詢查詢核身方式的List(原AA1107)
     *
     */
    queryScopeAuthorities(ReqBody: AA1115Req): Observable<ResAA1115> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryScopeAuthorities),
            ReqBody: ReqBody
        } as ReqAA1115
        const path = `${this.npBasePath}/AA1115`;
        return this.api.npPost<ResAA1115>(path, body);
    }

}

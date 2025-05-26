import { environment } from 'src/environments/environment';
import { ResAA0506, ReqAA0506 } from 'src/app/models/api/UtilService/aa0506.interface';
import { ResAA0505, ReqAA0505 } from 'src/app/models/api/UtilService/aa0505.interface';
import { ResAA0501, ReqAA0501 } from 'src/app/models/api/UtilService/aa0501.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
// import { AA0501Req } from 'src/app/models/api/UtilService/aa0501.interface';
import { TxID } from 'src/app/models/common.enum';
import { AA0501Req } from 'src/app/models/api/UtilService/aa0501.interface';
import { AA0505Req } from 'src/app/models/api/UtilService/aa0505.interface';
import { AA0506Req } from 'src/app/models/api/UtilService/aa0506.interface';
import { AA0507Req, ReqAA0507, ResAA0507 } from 'src/app/models/api/UtilService/aa0507.interface';
import { AA0511Req, ReqAA0511_v3, ResAA0511_v3 } from 'src/app/models/api/UtilService/aa0511_v3.interface';
import { ReqAA0510, ResAA0510 } from 'src/app/models/api/UtilService/aa0510.interface';
import { AA0525Req, ReqAA0525, ResAA0525, ResAA0525Before } from 'src/app/models/api/UtilService/aa0525.interface';
// import { AA0505Req } from 'src/app/models/api/UtilService/aa0505.interface';
// import { AA0506Req } from 'src/app/models/api/UtilService/aa0506.interface';
// import { AA0507Req, ReqAA0507, ResAA0507 } from 'src/app/models/api/UtilService/aa0507.interface';
// import { ResAA0510, ReqAA0510 } from 'src/app/models/api/UtilService/aa0510.interface';
// import { AA0509Req } from 'src/app/models/api/UtilService/aa0509.interface';
// import { AA0525Req, ReqAA0525, ResAA0525, ResAA0525Before } from 'src/app/models/api/UtilService/aa0525.interface';
// import { ReqAA0511_v3, AA0511Req, ResAA0511_v3 } from 'src/app/models/api/UtilService/aa0511_v3.interface';

@Injectable()
export class UtilService {

    constructor(private api: ApiBaseService, private tool: ToolService) {
    }

    public get dpBasePath(): string {
        return 'tsmpdpapi/api';
    }

    public get npBasePath(): string {
        return environment.isv4 ? 'dgrv4/11': 'tsmpdpaa/11';
    }

    /**
     * 忽略查無資料忽略查無資料
     * AA0501: 查詢NodeList
     * 查詢TSMP Node部署清單。TSMP Node可以固定部署多台，或是用docker方式動態加載部署，此API取得Node List可指定多少時間內有更新的清單，例如最近一小時內有運作的TSMP Nodes。
     * @param ReqBody
     */
    queryNodeList_ignore1298(ReqBody: AA0501Req): Observable<ResAA0501> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNodeList),
            ReqBody: ReqBody
        } as ReqAA0501
        const path = `${this.npBasePath}/AA0501`;
        return this.api.excuteNpPost_ignore1298<ResAA0501>(path, body);
    }

    /**
     * AA0501: 查詢NodeList
     * 查詢TSMP Node部署清單。TSMP Node可以固定部署多台，或是用docker方式動態加載部署，此API取得Node List可指定多少時間內有更新的清單，例如最近一小時內有運作的TSMP Nodes。
     * @param ReqBody
     */
    queryNodeList(ReqBody: AA0501Req): Observable<ResAA0501> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNodeList),
            ReqBody: ReqBody
        } as ReqAA0501
        const path = `${this.npBasePath}/AA0501`;
        return this.api.npPost<ResAA0501>(path, body);
    }

    /**
     * AA0505: QueryTaskStatus
     * 查詢排程執行狀態，包括啟動/停止Module部署的狀態。
     * @param body ReqAA0505
     */
    queryTaskStatus(ReqBody: AA0505Req): Observable<ResAA0505> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTaskStatus),
            ReqBody: ReqBody
        } as ReqAA0505
        const path = `${this.npBasePath}/AA0505`;
        return this.api.npPost<ResAA0505>(path, body);
    }

    /**
     * AA0506: getReportUrls
     * 預設取得所有報表Today的URLs。
     * @param req_0506 ReqAA0506
     */
    queryReportUrls(ReqBody: AA0506Req): Observable<ResAA0506> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryReportUrls),
            ReqBody: ReqBody
        } as ReqAA0506
        const path = `${this.npBasePath}/AA0506`;
        return this.api.npPost<ResAA0506>(path, body);
    }

    /**
     * v3
     * 忽略查無資料
     * AA0507: getAuditLog
     * 取得稽核紀錄。稽核紀錄即使用者在TSMP Admin Console上的操作紀錄，也就是API Log中屬於TSMPAA的API Log。
     * @param req_0507 Req_0507
     */
    queryAuditLog_ignore1298(ReqBody: AA0507Req): Observable<ResAA0507> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAuditLog),
            ReqBody: ReqBody
        } as ReqAA0507
        const path = `${this.npBasePath}/AA0507`;
        return this.api.excuteNpPost_ignore1298<ResAA0507>(path, body);
    }

    /**
     * v3
     * AA0507: getAuditLog
     * 取得稽核紀錄。稽核紀錄即使用者在TSMP Admin Console上的操作紀錄，也就是API Log中屬於TSMPAA的API Log。
     * @param req_0507 Req_0507
     */
    queryAuditLog(ReqBody: AA0507Req): Observable<ResAA0507> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAuditLog),
            ReqBody: ReqBody
        } as ReqAA0507
        const path = `${this.npBasePath}/AA0507`;
        return this.api.npPost<ResAA0507>(path, body);
    }

    /**
     * AA0511: getAuthCode
     * 外部Client(ex. Composer)取得授權碼AuthCode，即可由此授權碼向TSMP取得AccessToken。
     * AuthCode中會有user, client, expired等資訊
     * @param req_0511
     */
    getAuthCode_v3(req_0511: AA0511Req): Observable<ResAA0511_v3> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.getAuthCode),
            ReqBody: req_0511
        } as ReqAA0511_v3
        const path = `${this.npBasePath}/AA0511`;
        return this.api.npPost<ResAA0511_v3>(path, body);
    }

    getAcConf(): Observable<ResAA0510> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.getAcConf),
            ReqBody: {}
        } as ReqAA0510
        const path = `${this.npBasePath}/AA0510`;
        return this.api.npPost<ResAA0510>(path, body);
    }

    /**
     * before
     * AA0025: 查詢排程執行清單
     */
    queryTaskList_1_before(): Observable<ResAA0525Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTaskList_1),
            ReqBody: {}
        } as ReqAA0525;
        const path = `${this.npBasePath}/AA0525?before`;
        return this.api.npPost<ResAA0525Before>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0025: 查詢排程執行清單
     */
    queryTaskList_1_ignore1298(ReqBody: AA0525Req): Observable<ResAA0525> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTaskList_1),
            ReqBody: ReqBody
        } as ReqAA0525;
        const path = `${this.npBasePath}/AA0525`;
        return this.api.excuteNpPost_ignore1298<ResAA0525>(path, body);
    }

    /**
     * AA0025: 查詢排程執行清單
     */
    queryTaskList_1(ReqBody: AA0525Req): Observable<ResAA0525> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTaskList_1),
            ReqBody: ReqBody
        } as ReqAA0525;
        const path = `${this.npBasePath}/AA0525`;
        return this.api.npPost<ResAA0525>(path, body);
    }

}


import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0067Req, ResDPB0067, ReqDPB0067 } from 'src/app/models/api/RequisitionService/dpb0067.interface';
import { DPB0068Req, ResDPB0068, ReqDPB0068 } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { DPB0069Req, ResDPB0069, ReqDPB0069 } from 'src/app/models/api/RequisitionService/dpb0069.interface';
import { DPB0065Req, ResDPB0065, ReqDPB0065, ResDPB0065Before } from 'src/app/models/api/RequisitionService/dpb0065.interface';
import { DPB0066Req, ResDPB0066, ReqDPB0066, ResDPB0066Before } from 'src/app/models/api/RequisitionService/dpb0066.interface';

@Injectable({
    providedIn: 'root'
})
export class RequisitionService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0065: create申請單,含2種type,API申請(前台)、API上下架維護(後台)
     * @param ReqBody
     */
    createReq(ReqBody: DPB0065Req): Observable<ResDPB0065> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createReq),
            ReqBody: ReqBody
        } as ReqDPB0065
        const path = `${this.basePath}/DPB0065`;
        return this.api.npPost<ResDPB0065>(path, body);
    }

    createReq_beforer(): Observable<ResDPB0065Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createReq),
            ReqBody: {}
        } as ReqDPB0065
        const path = `${this.basePath}/DPB0065APIApplication?before`;
        return this.api.npPost<ResDPB0065Before>(path, body);
    }

    createReqOpenApiKey_beforer(): Observable<ResDPB0065Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createReq),
            ReqBody: {}
        } as ReqDPB0065
        const path = `${this.basePath}/DPB0065OpenAPIKey?before`;
        return this.api.npPost<ResDPB0065Before>(path, body);
    }

    createReqClientReq_beforer(): Observable<ResDPB0065Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createReq),
            ReqBody: {}
        } as ReqDPB0065
        const path = `${this.basePath}/DPB0065ClientReq?before`;
        return this.api.npPost<ResDPB0065Before>(path, body);
    }

    /**
     * DPB0066: ReSend / Update 申請單,含2種type
     * @param ReqBody
     */
    resendReq(ReqBody: DPB0066Req): Observable<ResDPB0066> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.resendReq),
            ReqBody: ReqBody
        } as ReqDPB0066
        const path = `${this.basePath}/DPB0066`;
        return this.api.npPost<ResDPB0066>(path, body);
    }

    /**
     * DPB0066: ReSend / Update 申請單,含2種type
     * @param ReqBody
     */
     resendReq_before(): Observable<ResDPB0066Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.resendReq),
            ReqBody: {}
        } as ReqDPB0066
        const path = `${this.basePath}/DPB0066OpenAPIKey?before2`;
        return this.api.npPost<ResDPB0066Before>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0067: query API上下架簽核清單
     * query個人申請單/審核工作單LikeList,含2種type
     */
    queryReqLikeList_ignore1298(ReqBody: DPB0067Req): Observable<ResDPB0067> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryReqLikeList),
            ReqBody: ReqBody
        } as ReqDPB0067
        const path = `${this.basePath}/DPB0067`;
        return this.api.excuteNpPost_ignore1298<ResDPB0067>(path, body);
    }

    /**
     * DPB0067: query API上下架簽核清單
     * query個人申請單/審核工作單LikeList,含2種type
     */
    queryReqLikeList(ReqBody: DPB0067Req): Observable<ResDPB0067> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryReqLikeList),
            ReqBody: ReqBody
        } as ReqDPB0067
        const path = `${this.basePath}/DPB0067`;
        return this.api.npPost<ResDPB0067>(path, body);
    }

    /**
     * DPB0068: query API上下架明細
     * query個人申請單/審核工作單ByPk,含2種type
     */
    queryReqByPk(ReqBody: DPB0068Req): Observable<ResDPB0068> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryReqByPk),
            ReqBody: ReqBody
        } as ReqDPB0068
        const path = `${this.basePath}/DPB0068`;
        return this.api.npPost<ResDPB0068>(path, body);
    }

    /**
     * DPB0069: 簽核歷程
     * 依申請單 id 查詢資料, 查詢所有的簽核歷呈
     */
    queryHistoryByPk(ReqBody: DPB0069Req): Observable<ResDPB0069> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryHistoryByPk),
            ReqBody: ReqBody
        } as ReqDPB0069
        const path = `${this.basePath}/DPB0069`;
        return this.api.npPost<ResDPB0069>(path, body);
    }
}

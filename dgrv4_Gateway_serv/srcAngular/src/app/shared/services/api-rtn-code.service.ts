
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0096Req, ResDPB0096, ReqDPB0096, ResDPB0096Before } from 'src/app/models/api/RtnCodeService/dpb0096.interface';
import { DPB0097Req, ResDPB0097, ReqDPB0097 } from 'src/app/models/api/RtnCodeService/dpb0097.interface';
import { DPB0098Req, ResDPB0098, ReqDPB0098 } from 'src/app/models/api/RtnCodeService/dpb0098.interface';
import { DPB0099Req, ResDPB0099, ReqDPB0099, ResDPB0099Before } from 'src/app/models/api/RtnCodeService/dpb0099.interface';
import { DPB0100Req, ResDPB0100, ReqDPB0100 } from 'src/app/models/api/RtnCodeService/dpb0100.interface';

@Injectable({
    providedIn: 'root'
})
export class RtnCodeService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * before
     * DPB0096: create申請單,含2種type,API申請(前台)、API上下架維護(後台)
     * @param ReqBody
     */
    createApiRtnCode_before(): Observable<ResDPB0096Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createApiRtnCode),
            ReqBody: {}
        } as ReqDPB0096
        const path = `${this.basePath}/DPB0096?before`;
        return this.api.npPost<ResDPB0096Before>(path, body);
    }

    /**
     * DPB0096: create申請單,含2種type,API申請(前台)、API上下架維護(後台)
     * @param ReqBody
     */
    createApiRtnCode(ReqBody: DPB0096Req): Observable<ResDPB0096> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createApiRtnCode),
            ReqBody: ReqBody
        } as ReqDPB0096
        const path = `${this.basePath}/DPB0096`;
        return this.api.npPost<ResDPB0096>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0097
     * @param ReqBody
     */
    queryApiRtnCodeList_ignore1298(ReqBody: DPB0097Req): Observable<ResDPB0097> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiRtnCodeList),
            ReqBody: ReqBody
        } as ReqDPB0097
        const path = `${this.basePath}/DPB0097`;
        return this.api.excuteNpPost_ignore1298<ResDPB0097>(path, body);
    }

    /**
     * DPB0097: ReSend / Update 申請單,含2種type
     * @param ReqBody
     */
    queryApiRtnCodeList(ReqBody: DPB0097Req): Observable<ResDPB0097> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiRtnCodeList),
            ReqBody: ReqBody
        } as ReqDPB0097
        const path = `${this.basePath}/DPB0097`;
        return this.api.npPost<ResDPB0097>(path, body);
    }

    /**
     * DPB0098: query API上下架簽核清單
     * query個人申請單/審核工作單LikeList,含2種type
     */
    qureyApiRtnCodeByPk(ReqBody: DPB0098Req): Observable<ResDPB0098> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.qureyApiRtnCodeByPk),
            ReqBody: ReqBody
        } as ReqDPB0098
        const path = `${this.basePath}/DPB0098`;
        return this.api.npPost<ResDPB0098>(path, body);
    }

    /**
     * before
     * DPB0099: query API上下架明細
     * query個人申請單/審核工作單ByPk,含2種type
     */
    updateApiRtnCode_before(): Observable<ResDPB0099Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateApiRtnCode),
            ReqBody: {}
        } as ReqDPB0099
        const path = `${this.basePath}/DPB0099?before`;
        return this.api.npPost<ResDPB0099Before>(path, body);
    }

    /**
     * DPB0099: query API上下架明細
     * query個人申請單/審核工作單ByPk,含2種type
     */
    updateApiRtnCode(ReqBody: DPB0099Req): Observable<ResDPB0099> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateApiRtnCode),
            ReqBody: ReqBody
        } as ReqDPB0099
        const path = `${this.basePath}/DPB0099`;
        return this.api.npPost<ResDPB0099>(path, body);
    }

    /**
     * DPB0100: 簽核歷程
     * 依申請單 id 查詢資料, 查詢所有的簽核歷呈
     */
    deleteApiRtnCode(ReqBody: DPB0100Req): Observable<ResDPB0100> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteApiRtnCode),
            ReqBody: ReqBody
        } as ReqDPB0100
        const path = `${this.basePath}/DPB0100`;
        return this.api.npPost<ResDPB0100>(path, body);
    }
}

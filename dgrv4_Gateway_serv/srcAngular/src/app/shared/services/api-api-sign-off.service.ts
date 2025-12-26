
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0071Req, ResDPB0071, ReqDPB0071 } from 'src/app/models/api/ApiSignOffService/dpb0071.interface';
import { DPB0072Req, ResDPB0072, ReqDPB0072 } from 'src/app/models/api/ApiSignOffService/dpb0072.interface';
import { DPB0073Req, ResDPB0073, ReqDPB0073 } from 'src/app/models/api/ApiSignOffService/dpb0073.interface';

@Injectable({
    providedIn: 'root'
})
export class ApiSignOffService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0071: 執行申請單/審核單狀態
     */
    signReq(ReqBody: DPB0071Req): Observable<ResDPB0071> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.signReq),
            ReqBody: ReqBody
        } as ReqDPB0071
        const path = `${this.basePath}/DPB0071`;
        return this.api.npPost<ResDPB0071>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0072: API架上現況查詢
     */
    queryApiDpStatusLikeList_ignore1298(ReqBody: DPB0072Req): Observable<ResDPB0072> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiDpStatusLikeList),
            ReqBody: ReqBody
        } as ReqDPB0072
        const path = `${this.basePath}/DPB0072`;
        return this.api.excuteNpPost_ignore1298<ResDPB0072>(path, body);
    }

    /**
     * DPB0072: API架上現況查詢
     */
    queryApiDpStatusLikeList(ReqBody: DPB0072Req): Observable<ResDPB0072> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiDpStatusLikeList),
            ReqBody: ReqBody
        } as ReqDPB0072
        const path = `${this.basePath}/DPB0072`;
        return this.api.npPost<ResDPB0072>(path, body);
    }

    /**
     * DPB0073: API開放權限設定
     * 可異動API. public_flag 欄位 , 異動完成 body 不會回傳資料, 只有 rtn code = 0000, 允許指定多筆 API 批次異動.
     */
    setApiPublicFlag(ReqBody: DPB0073Req): Observable<ResDPB0073> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.setApiPublicFlag),
            ReqBody: ReqBody
        } as ReqDPB0073
        const path = `${this.basePath}/DPB0073`;
        return this.api.npPost<ResDPB0073>(path, body);
    }
}

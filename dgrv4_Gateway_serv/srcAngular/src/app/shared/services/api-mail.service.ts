import { environment } from 'src/environments/environment';

import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import { DPB0116Req, ReqDPB0116, ResDPB0116, ResDPB0116Before } from 'src/app/models/api/MailService/dpb0116.interface';
import { DPB0117Req, ReqDPB0117, ResDPB0117 } from 'src/app/models/api/MailService/dpb0117.interface';

@Injectable({
    providedIn: 'root'
})
export class MailService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) { }

    /**
     * before
     * DPB0116: 依照建立日期及關鍵字及傳送結果查詢寄件歷程資料
     * @param ReqBody
     */
    queryMailLogList_before(): Observable<ResDPB0116Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMailLogList),
            ReqBody: {}
        } as ReqDPB0116
        const path = `${this.basePath}/DPB0116?before`;
        return this.api.npPost<ResDPB0116Before>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0116: 依照建立日期及關鍵字及傳送結果查詢寄件歷程資料
     */
    queryMailLogList_ignore1298(ReqBody: DPB0116Req): Observable<ResDPB0116> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMailLogList),
            ReqBody: ReqBody
        } as ReqDPB0116
        const path = `${this.basePath}/DPB0116`;
        return this.api.excuteNpPost_ignore1298<ResDPB0116>(path, body);
    }

    /**
     * DPB0116: 依照建立日期及關鍵字及傳送結果查詢寄件歷程資料
     */
    queryMailLogList(ReqBody: DPB0116Req): Observable<ResDPB0116> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMailLogList),
            ReqBody: ReqBody
        } as ReqDPB0116
        const path = `${this.basePath}/DPB0116`;
        return this.api.npPost<ResDPB0116>(path, body);
    }

    /**
     * DPB0117: 查詢寄件歷程的詳細資料
     */
    queryMailLogDetail(ReqBody: DPB0117Req): Observable<ResDPB0117> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMailLogDetail),
            ReqBody: ReqBody
        } as ReqDPB0117
        const path = `${this.basePath}/DPB0117`;
        return this.api.npPost<ResDPB0117>(path, body);
    }

}

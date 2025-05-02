
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0100Req, ResDPB0100, ReqDPB0100 } from 'src/app/models/api/RtnCodeService/dpb0100.interface';
import { DPB0106Req, ResDPB0106, ReqDPB0106 } from 'src/app/models/api/EventService/dpb0106.interface';
import { DPB0107Req, ResDPB0107, ReqDPB0107 } from 'src/app/models/api/EventService/dpb0107.interface';
import { DPB0108Req, ResDPB0108, ReqDPB0108 } from 'src/app/models/api/EventService/dpb0108.interface';
import { DPB0109Req, ResDPB0109, ReqDPB0109 } from 'src/app/models/api/EventService/dpb0109.interface';

@Injectable({
    providedIn: 'root'
})
export class EventService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * 忽略查無資料
     * DPB0106: 查詢事件清單依日期Like
     * 依建立日期 和 關鍵字(不分大小寫) 模糊搜尋出事件清單
     */
    queryEventByDateLike_ignore1298(ReqBody: DPB0106Req): Observable<ResDPB0106> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryEventByDateLike),
            ReqBody: ReqBody
        } as ReqDPB0106
        const path = `${this.basePath}/DPB0106`;
        return this.api.excuteNpPost_ignore1298<ResDPB0106>(path, body);
    }

    /**
     * DPB0106: 查詢事件清單依日期Like
     * 依建立日期 和 關鍵字(不分大小寫) 模糊搜尋出事件清單
     */
    queryEventByDateLike(ReqBody: DPB0106Req): Observable<ResDPB0106> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryEventByDateLike),
            ReqBody: ReqBody
        } as ReqDPB0106
        const path = `${this.basePath}/DPB0106`;
        return this.api.npPost<ResDPB0106>(path, body);
    }

    /**
     * DPB0107: 查詢事件依PK
     * 依 Event PK 查出該筆事件的詳細資料
     */
    queryEventByPk(ReqBody: DPB0107Req): Observable<ResDPB0107> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryEventByPk),
            ReqBody: ReqBody
        } as ReqDPB0107
        const path = `${this.basePath}/DPB0107`;
        return this.api.npPost<ResDPB0107>(path, body);
    }

    /**
     * DPB0108: 更新事件是否保留依PK
     * 依 Event PK 查出該筆事件的詳細資料
     */
    keepEventByPk(ReqBody: DPB0108Req): Observable<ResDPB0108> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.keepEventByPk),
            ReqBody: ReqBody
        } as ReqDPB0108
        const path = `${this.basePath}/DPB0108`;
        return this.api.npPost<ResDPB0108>(path, body);
    }

    /**
     * DPB0109: 更新事件是否封存依PK
     * 依 Event PK 查出該筆事件的詳細資料
     */
    archiveEventByPk(ReqBody: DPB0109Req): Observable<ResDPB0109> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.archiveEventByPk),
            ReqBody: ReqBody
        } as ReqDPB0109
        const path = `${this.basePath}/DPB0109`;
        return this.api.npPost<ResDPB0109>(path, body);
    }
}

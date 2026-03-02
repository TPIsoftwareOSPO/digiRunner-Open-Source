import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { RespDPB0042, DPB0042Req, ReqDPB0042 } from 'src/app/models/api/NewsService/dpb0042.interface';
import { RespDPB0043, DPB0043Req, ReqDPB0043 } from 'src/app/models/api/NewsService/dpb0043.interface';
import { DPB0044Req, RespDPB0044, ReqDPB0044 } from 'src/app/models/api/NewsService/dpb0044.interface';
import { RespDPB0045, DPB0045Req, ReqDPB0045 } from 'src/app/models/api/NewsService/dpb0045.interface';
import { RespDPB0046, ReqDPB0046, DPB0046Req } from 'src/app/models/api/NewsService/dpb0046.interface';

@Injectable({
    providedIn: 'root'
})
export class NewsService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0042: 公告新增
     * 於後台中新增一筆公告消息
     */
    createNews_v3_4(ReqBody: DPB0042Req): Observable<RespDPB0042> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createNews_v3_4),
            ReqBody: ReqBody
        } as ReqDPB0042
        const path = `${this.basePath}/DPB0042`;
        return this.api.npPost<RespDPB0042>(path, body);
    }

    /**
     * DPB0043: 公告更新
     * 於後台中update一筆公告消息
     */
    updateNews(ReqBody: DPB0043Req): Observable<RespDPB0043> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateNews),
            ReqBody: ReqBody
        } as ReqDPB0043
        const path = `${this.basePath}/DPB0043`;
        return this.api.npPost<RespDPB0043>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0044: 公告列表
     * 於後台中query 並含分頁效果的表格
     */
    queryNewsLike_v3_4_ignore1298(ReqBody: DPB0044Req): Observable<RespDPB0044> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNewsLike_v3_4),
            ReqBody: ReqBody
        } as ReqDPB0044
        const path = `${this.basePath}/DPB0044`;
        return this.api.excuteNpPost_ignore1298<RespDPB0044>(path, body);
    }

    /**
     * DPB0044: 公告列表
     * 於後台中query 並含分頁效果的表格
     */
    queryNewsLike_v3_4(ReqBody: DPB0044Req): Observable<RespDPB0044> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNewsLike_v3_4),
            ReqBody: ReqBody
        } as ReqDPB0044
        const path = `${this.basePath}/DPB0044`;
        return this.api.npPost<RespDPB0044>(path, body);
    }


    /**
     * DPB0045: 公告詳情
     * 於後台中query by Id
     */
    queryNewsById(ReqBody: DPB0045Req): Observable<RespDPB0045> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNewsById),
            ReqBody: ReqBody
        } as ReqDPB0045
        const path = `${this.basePath}/DPB0045`;
        return this.api.npPost<RespDPB0045>(path, body);
    }


    /**
     * DPB0046: 公告刪除
     * 於後台中delete by Id, 支援多筆一次更新。
     * 實際作法: update 狀態=停用
     */
    deleteNews_v3_4(ReqBody: DPB0046Req): Observable<RespDPB0046> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteNews_v3_4),
            ReqBody: ReqBody
        } as ReqDPB0046
        const path = `${this.basePath}/DPB0046`;
        return this.api.npPost<RespDPB0046>(path, body);
    }
}

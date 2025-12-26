
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0075Req, ResDPB0075, ReqDPB0075 } from 'src/app/models/api/LovService/dpb0075.interface';
import { DPB0076Req, ResDPB0076, ReqDPB0076 } from 'src/app/models/api/LovService/dpb0076.interface';

@Injectable({
    providedIn: 'root'
})
export class LovService {

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
     * DPB0075: Keyword Search API (API系列 /主題分類系列 /所屬單位) (分為己上架/未上架)
     * 提供後台開窗查詢, 需要分頁

     */
    queryApiLov_ignore1298(ReqBody: DPB0075Req): Observable<ResDPB0075> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiLov),
            ReqBody: ReqBody
        } as ReqDPB0075
        const path = `${this.basePath}/DPB0075`;
        return this.api.excuteNpPost_ignore1298<ResDPB0075>(path, body);
    }

    /**
     * DPB0075: Keyword Search API (API系列 /主題分類系列 /所屬單位) (分為己上架/未上架)
     * 提供後台開窗查詢, 需要分頁

     */
    queryApiLov(ReqBody: DPB0075Req): Observable<ResDPB0075> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiLov),
            ReqBody: ReqBody
        } as ReqDPB0075
        const path = `${this.basePath}/DPB0075`;
        return this.api.npPost<ResDPB0075>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0076: 主題分類 Lov
     * 系統中所有需要選取並代ThemeId 的開窗功能, 作成下拉複選, 回傳的內容需分頁
     */
    queryThemeLov_ignore1298(ReqBody: DPB0076Req): Observable<ResDPB0076> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeLov),
            ReqBody: ReqBody
        } as ReqDPB0076
        const path = `${this.basePath}/DPB0076`;
        return this.api.excuteNpPost_ignore1298<ResDPB0076>(path, body);
    }

    /**
     * DPB0076: 主題分類 Lov
     * 系統中所有需要選取並代ThemeId 的開窗功能, 作成下拉複選, 回傳的內容需分頁
     */
    queryThemeLov(ReqBody: DPB0076Req): Observable<ResDPB0076> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeLov),
            ReqBody: ReqBody
        } as ReqDPB0076
        const path = `${this.basePath}/DPB0076`;
        return this.api.npPost<ResDPB0076>(path, body);
    }
}

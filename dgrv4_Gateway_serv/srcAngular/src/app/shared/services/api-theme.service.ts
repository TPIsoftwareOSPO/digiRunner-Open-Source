import { ToolService } from 'src/app/shared/services/tool.service';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { DPB0019Req, RespDPB0019, ReqDPB0019 } from 'src/app/models/api/ThemeService/dpb0019.interface';
import { TxID } from 'src/app/models/common.enum';
import { DPB0020Req, RespDPB0020, ReqDPB0020 } from 'src/app/models/api/ThemeService/dpb0020.interface';
import { DPB0023Req, RespDPB0023, ReqDPB0023 } from 'src/app/models/api/ThemeService/dpb0023.interface';
import { DPB0021Req, RespDPB0021, ReqDPB0021 } from 'src/app/models/api/ThemeService/dpb0021.interface';
import { DPB0022Req, RespDPB0022, ReqDPB0022 } from 'src/app/models/api/ThemeService/dpb0022.interface';
import { DPB0053Req, RespDPB0053, ReqDPB0053 } from 'src/app/models/api/ThemeService/dpb0053.interface';
import { DPB0054Req, RespDPB0054, ReqDPB0054 } from 'src/app/models/api/ThemeService/dpb0054.interface';
import { DPB0055Req, RespDPB0055, ReqDPB0055 } from 'src/app/models/api/ThemeService/dpb0055.interface';
import { DPB0056Req, RespDPB0056, ReqDPB0056 } from 'src/app/models/api/ThemeService/dpb0056.interface';
import { DPB0057Req, RespDPB0057, ReqDPB0057 } from 'src/app/models/api/ThemeService/dpb0057.interface';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0019: 主題分類新增
     * 主要新增一筆資料至 Table, 及新增一筆 file 至Table與 storage,
     */
    addTheme(ReqBody: DPB0019Req): Observable<RespDPB0019> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addTheme),
            ReqBody: ReqBody
        } as ReqDPB0019
        const path = `${this.basePath}/DPB0019`;
        return this.api.npPost<RespDPB0019>(path, body);
    }

    /**
     * DPB0020: 主題分類Like查詢
     * 查詢主題分類表[TSMP_DP_THEME_CATEGORY], Like 欄位:[主題分類名稱] [API Key] [API來源] [模組名稱] [API名稱] [API版本] [API說明] , 每一筆主題包含多筆api, order by createDateTime Desc, PK DESC
     */
    queryThemeLikeList(ReqBody: DPB0020Req): Observable<RespDPB0020> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeLikeList),
            ReqBody: ReqBody
        } as ReqDPB0020
        const path = `${this.basePath}/DPB0020`;
        return this.api.npPost<RespDPB0020>(path, body);
    }

    /**
     * DPB0021: 主題分類查詢byId
     * 查找出一筆主題資料及它以下包含的API資料
     * @param ReqBody
     */
    queryThemeById(ReqBody: DPB0021Req): Observable<RespDPB0021> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeById),
            ReqBody: ReqBody
        } as ReqDPB0021
        const path = `${this.basePath}/DPB0021`;
        return this.api.npPost<RespDPB0021>(path, body);
    }

    /**
     * DPB0022: 主題分類更新byId
     * 根據 DPB0021 查出來的資料來做主表及mapp表的修改
     */
    updateThemeById(ReqBody: DPB0022Req): Observable<RespDPB0022> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateThemeById),
            ReqBody: ReqBody
        } as ReqDPB0022
        const path = `${this.basePath}/DPB0022`;
        return this.api.npPost<RespDPB0022>(path, body);
    }

    /**
     * DPB0023: 主題分類刪除byId
     * 刪除主表一筆資料及其關連表的資料
     */
    deleteThemeById(ReqBody: DPB0023Req): Observable<RespDPB0023> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteThemeById),
            ReqBody: ReqBody
        } as ReqDPB0023
        const path = `${this.basePath}/DPB0023`;
        return this.api.npPost<RespDPB0023>(path, body);
    }

    /**
     * DPB0053: 新增主題
     * 新增一筆主題, 且附上圖片, 圖片上傳檔案使用 "Multipart X JSON整合設計", 也就是新增主題會使用2支API。
     * 使用Multipart，先呼叫上傳檔案的API，取得到暫存檔名後，在實際呼叫新增的API。
     */
    createTheme(ReqBody: DPB0053Req): Observable<RespDPB0053> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createTheme),
            ReqBody: ReqBody
        } as ReqDPB0053
        const path = `${this.basePath}/DPB0053`;
        return this.api.npPost<RespDPB0053>(path, body);
    }

    /**
     * DPB0054: 更新主題
     * update一筆主題, 且替換圖片, 圖片上傳檔案使用 "Multipart X JSON整合設計", 也就是新增主題會使用2支API
     */
    updateTheme(ReqBody: DPB0054Req): Observable<RespDPB0054> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTheme),
            ReqBody: ReqBody
        } as ReqDPB0054
        const path = `${this.basePath}/DPB0054`;
        return this.api.npPost<RespDPB0054>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0055: 查詢like list主題
     * smart search + 分頁查詢
     */
    queryThemeLikeList_1_ignore1298(ReqBody: DPB0055Req): Observable<RespDPB0055> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeLikeList_1),
            ReqBody: ReqBody
        } as ReqDPB0055
        const path = `${this.basePath}/DPB0055`;
        return this.api.excuteNpPost_ignore1298<RespDPB0055>(path, body);
    }

    /**
     * DPB0055: 查詢like list主題
     * smart search + 分頁查詢
     */
    queryThemeLikeList_1(ReqBody: DPB0055Req): Observable<RespDPB0055> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeLikeList_1),
            ReqBody: ReqBody
        } as ReqDPB0055
        const path = `${this.basePath}/DPB0055`;
        return this.api.npPost<RespDPB0055>(path, body);
    }

    /**
     * DPB0056: 查詢主題
     * 找出一筆資料, 供 update 使用
     */
    queryThemeByPk(ReqBody: DPB0056Req): Observable<RespDPB0056> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryThemeByPk),
            ReqBody: ReqBody
        } as ReqDPB0056
        const path = `${this.basePath}/DPB0056`;
        return this.api.npPost<RespDPB0056>(path, body);
    }

    /**
     * DPB0057: 刪除主題
     * 於後台中delete by Id, 支援多筆一次更新
     */
    deleteTheme(ReqBody: DPB0057Req): Observable<RespDPB0057> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteTheme),
            ReqBody: ReqBody
        } as ReqDPB0057
        const path = `${this.basePath}/DPB0057`;
        return this.api.npPost<RespDPB0057>(path, body);
    }
}

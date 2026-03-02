
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0090Req, ReqDPB0090, ResDPB0090 } from 'src/app/models/api/OpenApiService/dpb0090.interface';
import { DPB0091Req, ResDPB0091, ReqDPB0091 } from 'src/app/models/api/OpenApiService/dpb0091.interface';
import { DPB0092Req, ResDPB0092, ReqDPB0092 } from 'src/app/models/api/OpenApiService/dpb0092.intrface';
import { DPB0093Req, ResDPB0093, ReqDPB0093 } from 'src/app/models/api/OpenApiService/dpb0093.interface';
import { DPB0094Req, RespDPB0094, ReqDPB0094, RespDPB0094Before } from 'src/app/models/api/OpenApiService/dpb0094.interface';
import { DPB0095Req, ReqDPB0095, RespDPB0095 } from 'src/app/models/api/OpenApiService/dpb0095.interface';

@Injectable({
    providedIn: 'root'
})
export class OpenApiKeyService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0090: 查詢Open API Key依Client ID
     * 查詢該 Client ID 的 Open API KEY 清單
     */
    queryOpenApiKeyByClientId(ReqBody: DPB0090Req): Observable<ResDPB0090> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryOpenApiKeyByClientId),
            ReqBody: ReqBody
        } as ReqDPB0090
        const path = `${this.basePath}/DPB0090`;
        return this.api.npPost<ResDPB0090>(path, body);
    }

    /**
     * DPB0091: 查詢Open API Key明細依PK
     * 指定 Client 的 Open API Key ID 查找該 Open API Key 的Deatil
     */
    queryOpenApiKeyDetailByPk(ReqBody: DPB0091Req): Observable<ResDPB0091> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryOpenApiKeyDetailByPk),
            ReqBody: ReqBody
        } as ReqDPB0091
        const path = `${this.basePath}/DPB0091`;
        return this.api.npPost<ResDPB0091>(path, body);
    }

    /**
     * DPB0092: 填寫Open API Key申請單
     * 填寫申請單草稿
     */
    writeOpenApiKeyReq(ReqBody: DPB0092Req): Observable<ResDPB0092> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.writeOpenApiKeyReq),
            ReqBody: ReqBody
        } as ReqDPB0092
        const path = `${this.basePath}/DPB0092`;
        return this.api.npPost<ResDPB0092>(path, body);
    }

    /**
     * DPB0093: API Like 查詢
     * 提供前/後台開窗查詢 API, 需要分頁
     * 所有組織、上下架的API都可以查到
     */
    queryApiLikeList(ReqBody: DPB0093Req): Observable<ResDPB0093> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryApiLikeList),
            ReqBody: ReqBody
        } as ReqDPB0093
        const path = `${this.basePath}/DPB0093`;
        return this.api.npPost<ResDPB0093>(path, body);
    }

    /**
     * before
     * DPB0094: 查詢Open API Key 清單依日期Like
     */
    queryOpenApiKeyByDateAndLike_before(): Observable<RespDPB0094Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryOpenApiKeyByDateAndLike),
            ReqBody: {}
        } as ReqDPB0094
        const path = `${this.basePath}/DPB0094?before`;
        return this.api.npPost<RespDPB0094Before>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0094: 查詢Open API Key 清單依日期Like
     * 依建立日期 和 關鍵字(不分大小寫)模糊搜尋出 Open API Key 清單
     * 模糊搜尋欄位 : CLIENT_ID、 CLIENT_NAME、 CLIENT_ALIAS
     * 排序 : tsmp_open_apikey.create_date_time desc , tsmp_open_apikey.open_apikey_id desc   (Open API Key 建立時間 desc, Open API Key ID desc)
     * 狀態 : 若啟用的Open API Key效期已過,在畫面狀態顯示"啟用(逾期)"
     * 結束日期 : 6/31 表示查詢為小於 7/1
     * createDateTime 或 updateDateTime 在日期範圍內
     */
    queryOpenApiKeyByDateAndLike_ignore1298(ReqBody: DPB0094Req): Observable<RespDPB0094> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryOpenApiKeyByDateAndLike),
            ReqBody: ReqBody
        } as ReqDPB0094
        const path = `${this.basePath}/DPB0094`;
        return this.api.excuteNpPost_ignore1298<RespDPB0094>(path, body);
    }

    /**
     * DPB0094: 查詢Open API Key 清單依日期Like
     * 依建立日期 和 關鍵字(不分大小寫)模糊搜尋出 Open API Key 清單
     * 模糊搜尋欄位 : CLIENT_ID、 CLIENT_NAME、 CLIENT_ALIAS
     * 排序 : tsmp_open_apikey.create_date_time desc , tsmp_open_apikey.open_apikey_id desc   (Open API Key 建立時間 desc, Open API Key ID desc)
     * 狀態 : 若啟用的Open API Key效期已過,在畫面狀態顯示"啟用(逾期)"
     * 結束日期 : 6/31 表示查詢為小於 7/1
     * createDateTime 或 updateDateTime 在日期範圍內
     */
    queryOpenApiKeyByDateAndLike(ReqBody: DPB0094Req): Observable<RespDPB0094> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryOpenApiKeyByDateAndLike),
            ReqBody: ReqBody
        } as ReqDPB0094
        const path = `${this.basePath}/DPB0094`;
        return this.api.npPost<RespDPB0094>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0095: 依日期範圍查找 CA List
     * 查詢主表: TSMP_CLIENT
     * 依照 關鍵字(用戶端帳號、用戶端代號、用戶端名稱) 及 會員資格狀態 = 2 搜尋, 並以 用戶端帳號 asc 排序
     */
    queryClientListByRegStatusLike_ignore1298(ReqBody: DPB0095Req): Observable<RespDPB0095> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByRegStatusLike),
            ReqBody: ReqBody
        } as ReqDPB0095
        const path = `${this.basePath}/DPB0095`;
        return this.api.excuteNpPost_ignore1298<RespDPB0095>(path, body);
    }

    /**
     * DPB0095: 依日期範圍查找 CA List
     * 查詢主表: TSMP_CLIENT
     * 依照 關鍵字(用戶端帳號、用戶端代號、用戶端名稱) 及 會員資格狀態 = 2 搜尋, 並以 用戶端帳號 asc 排序
     */
    queryClientListByRegStatusLike(ReqBody: DPB0095Req): Observable<RespDPB0095> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryClientListByRegStatusLike),
            ReqBody: ReqBody
        } as ReqDPB0095
        const path = `${this.basePath}/DPB0095`;
        return this.api.npPost<RespDPB0095>(path, body);
    }

}

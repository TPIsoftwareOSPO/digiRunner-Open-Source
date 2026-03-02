import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { DPB0027Req, ResDPB0027, ReqDPB0027 } from 'src/app/models/api/FaqService/dpb0027.interface';
import { TxID } from 'src/app/models/common.enum';
import { DPB0030Req, ResDPB0030, ReqDPB0030 } from 'src/app/models/api/FaqService/dpb0030.interface';
import { ResDPB0029, DPB0029Req, ReqDPB0029 } from 'src/app/models/api/FaqService/dpb0029.interface';
import { DPB0028Req, ResDPB0028, ReqDPB0028 } from 'src/app/models/api/FaqService/dpb0028.interface';
import { DPB0026Req, ResDPB0026, ReqDPB0026 } from 'src/app/models/api/FaqService/dpb0026.interface';

@Injectable({
    providedIn: 'root'
})
export class FaqService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0026: 常見問答新增
     *
     */
    addFaq(ReqBody: DPB0026Req): Observable<ResDPB0026> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addFaq),
            ReqBody: ReqBody
        } as ReqDPB0026
        const path = `${this.basePath}/DPB0026`;
        return this.api.npPost<ResDPB0026>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0027: 常見問答Like查詢
     * 查詢主表[TSMP_DP_FAQ_QUESTION]做分頁, order by 排序+PK
     */
    queryFaqLikeList_0_ignore1298(ReqBody: DPB0027Req): Observable<ResDPB0027> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryFaqLikeList_0),
            ReqBody: ReqBody
        } as ReqDPB0027
        const path = `${this.basePath}/DPB0027`;
        return this.api.excuteNpPost_ignore1298<ResDPB0027>(path, body);
    }

    /**
     * DPB0027: 常見問答Like查詢
     * 查詢主表[TSMP_DP_FAQ_QUESTION]做分頁, order by 排序+PK
     */
    queryFaqLikeList_0(ReqBody: DPB0027Req): Observable<ResDPB0027> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryFaqLikeList_0),
            ReqBody: ReqBody
        } as ReqDPB0027
        const path = `${this.basePath}/DPB0027`;
        return this.api.npPost<ResDPB0027>(path, body);
    }

    /**
     * DPB0028: 常見問答更新byId
     * 更新一筆資料, 檔案操作提供更新檔案, 移除檔案, 若原來資料沒有檔案時則新增檔案
     */
    updateFaqById(ReqBody: DPB0028Req): Observable<ResDPB0028> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateFaqById),
            ReqBody: ReqBody
        } as ReqDPB0028
        const path = `${this.basePath}/DPB0028`;
        return this.api.npPost<ResDPB0028>(path, body);
    }

    /**
     * DPB0029: 常見問答查詢byId
     * 查找出一筆資料, 提供給更新畫面使用
     */
    queryFaqById(ReqBody: DPB0029Req): Observable<ResDPB0029> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryFaqById),
            ReqBody: ReqBody
        } as ReqDPB0029
        const path = `${this.basePath}/DPB0029`;
        return this.api.npPost<ResDPB0029>(path, body);
    }

    /**
     * DPB0030: 常見問答刪除byId
     * 刪除一筆資料
     */
    deleteFaqById(ReqBody: DPB0030Req): Observable<ResDPB0030> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteFaqById),
            ReqBody: ReqBody
        } as ReqDPB0030
        const path = `${this.basePath}/DPB0030`;
        return this.api.npPost<ResDPB0030>(path, body);
    }
}

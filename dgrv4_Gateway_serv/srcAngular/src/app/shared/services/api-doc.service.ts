
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { DPB0040Req, ResDPB0040, ReqDPB0040 } from 'src/app/models/api/DocService/dpb0040.interface';
import { TxID } from 'src/app/models/common.enum';
import { DPB0041Req, ResDPB0041, ReqDPB0041 } from 'src/app/models/api/DocService/dpb0041.interface';

@Injectable({
    providedIn: 'root'
})
export class DocService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * 忽略查無資料忽略查無資料
     * DPB0040: 查詢可用Module
     * 分頁查詢Tsmp_API_MODULE , Like 1欄位
     * moduleName
     * where active=true
     * ps: 若 moduleNanme 存在於 TSMP_DP_DENIED_MODULE者, 表示不公開, 可在 service 中另外查找
     */
    queryModuleLikeList_ignore1298(ReqBody: DPB0040Req): Observable<ResDPB0040> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleLikeList),
            ReqBody: ReqBody
        } as ReqDPB0040
        const path = `${this.basePath}/DPB0040`;
        return this.api.excuteNpPost_ignore1298<ResDPB0040>(path, body);
    }

    /**
     * DPB0040: 查詢可用Module
     * 分頁查詢Tsmp_API_MODULE , Like 1欄位
     * moduleName
     * where active=true
     * ps: 若 moduleNanme 存在於 TSMP_DP_DENIED_MODULE者, 表示不公開, 可在 service 中另外查找
     */
    queryModuleLikeList(ReqBody: DPB0040Req): Observable<ResDPB0040> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleLikeList),
            ReqBody: ReqBody
        } as ReqDPB0040
        const path = `${this.basePath}/DPB0040`;
        return this.api.npPost<ResDPB0040>(path, body);
    }

    /**
     * DPB0041: 黑名單 Save
     * 清除TSMP_DP_DENIED_MODULE原有資料, 重新寫入有勾選的 moduleName, 存檔失敗需要 rollback
     */
    saveDeniedModule(ReqBody: DPB0041Req): Observable<ResDPB0041> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.saveDeniedModule),
            ReqBody: ReqBody
        } as ReqDPB0041
        const path = `${this.basePath}/DPB0041`;
        return this.api.npPost<ResDPB0041>(path, body);
    }
}

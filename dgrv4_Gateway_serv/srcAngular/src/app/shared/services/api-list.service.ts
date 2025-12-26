
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0047Req, ResDPB0047, ReqDPB0047 } from 'src/app/models/api/ListService/dpb0047.interface';

@Injectable({
    providedIn: 'root'
})
export class ListService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0047: query XX類型 Lov List
     * 代入 TSMP_DP_ITEMS.itemNo, 取出此類型下的子類型
     */
    querySubItemsByItemNo(ReqBody: DPB0047Req): Observable<ResDPB0047> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySubItemsByItemNo),
            ReqBody: ReqBody
        } as ReqDPB0047
        const path = `${this.basePath}/DPB0047`;
        return this.api.npPost<ResDPB0047>(path, body);
    }

    queryNewsStatusList(ReqBody: DPB0047Req): Observable<ResDPB0047> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySubItemsByItemNo),
            ReqBody: ReqBody
        } as ReqDPB0047
        const path = `${this.basePath}/DPB0048`;
        return this.api.npPost<ResDPB0047>(path, body);
    }
}

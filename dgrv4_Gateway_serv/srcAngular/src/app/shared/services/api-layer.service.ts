
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { ResDPB0064, ReqDPB0064 } from 'src/app/models/api/LayerService/dpb0064.interface';
import { DPB0063Req, ResDPB0063, ReqDPB0063 } from 'src/app/models/api/LayerService/dpb0063.interface';

@Injectable({
    providedIn: 'root'
})
export class LayerService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0063: save關卡與角色，優先使用status取代create/delete/clear操作
     */
    saveLayer(ReqBody: DPB0063Req): Observable<ResDPB0063> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.saveLayer),
            ReqBody: ReqBody
        } as ReqDPB0063
        const path = `${this.basePath}/DPB0063`;
        return this.api.npPost<ResDPB0063>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0064: query全表
     * find All + Parser
     */
    queryAllLayer_ignore1298(): Observable<ResDPB0064> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAllLayer),
            ReqBody: {}
        } as ReqDPB0064
        const path = `${this.basePath}/DPB0064`;
        return this.api.excuteNpPost_ignore1298<ResDPB0064>(path, body);
    }

    /**
     * DPB0064: query全表
     * find All + Parser
     */
    queryAllLayer(): Observable<ResDPB0064> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAllLayer),
            ReqBody: {}
        } as ReqDPB0064
        const path = `${this.basePath}/DPB0064`;
        return this.api.npPost<ResDPB0064>(path, body);
    }

}

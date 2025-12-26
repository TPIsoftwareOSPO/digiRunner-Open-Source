import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0006Req, ResDPB0006, ReqDPB0006 } from 'src/app/models/api/MemberService/dpb0006.interface';

@Injectable({
    providedIn: 'root'
})
export class MemberService {

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
     * DPB0006: 會員資格審歷程 + 砍除過期且退回申請的會員
     * 查詢已狀態=2：放行，3：退回, order by updateDateTime desc, 起迄範圍以 updateDateTime為條件
     */
    queryMemberHistory_ignore1298(ReqBody: DPB0006Req): Observable<ResDPB0006> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMemberHistory),
            ReqBody: ReqBody
        } as ReqDPB0006
        const path = `${this.basePath}/DPB0006`;
        return this.api.excuteNpPost_ignore1298<ResDPB0006>(path, body);
    }

    /**
     * DPB0006: 會員資格審歷程 + 砍除過期且退回申請的會員
     * 查詢已狀態=2：放行，3：退回, order by updateDateTime desc, 起迄範圍以 updateDateTime為條件
     */
    queryMemberHistory(ReqBody: DPB0006Req): Observable<ResDPB0006> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryMemberHistory),
            ReqBody: ReqBody
        } as ReqDPB0006
        const path = `${this.basePath}/DPB0006`;
        return this.api.npPost<ResDPB0006>(path, body);
    }

}

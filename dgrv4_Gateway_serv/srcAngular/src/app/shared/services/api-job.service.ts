import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0058Req, ResDPB0058, ReqDPB0058 } from 'src/app/models/api/JobService/dpb0058.interface';
import { DPB0059Req, ResDPB0059, ReqDPB0059 } from 'src/app/models/api/JobService/dpb0059.interface';
import { DPB0060Req, ResDPB0060, ReqDPB0060 } from 'src/app/models/api/JobService/dpb0060.interface';
import { DPB0061Req, ResDPB0061, ReqDPB0061 } from 'src/app/models/api/JobService/dpb0061.interface';
import { DPB0062Req, ResDPB0062, ReqDPB0062 } from 'src/app/models/api/JobService/dpb0062.interface';
import { ResDPB0121 } from 'src/app/models/api/JobService/dpb0121.interface';
import { ajax, AjaxResponse } from 'rxjs/ajax';
import { ToolService } from 'src/app/shared/services/tool.service';


@Injectable({
    providedIn: 'root'
})
export class JobService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService,
        private tool:ToolService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * 忽略查無資料
     * DPB0058: refresh MemList後, 檢查本機MemList是否有可執行工作 +列出工作清單
     * 檢查本機MemList是否有可執行工作, 若符合執行的job則排入執行工作
     */
    queryJobLikeList_ignore1298(ReqBody: DPB0058Req): Observable<ResDPB0058> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryJobLikeList),
            ReqBody: ReqBody
        } as ReqDPB0058
        const path = `${this.basePath}/DPB0058`;
        return this.api.excuteNpPost_ignore1298<ResDPB0058>(path, body);
    }

    /**
     * DPB0058: refresh MemList後, 檢查本機MemList是否有可執行工作 +列出工作清單
     * 檢查本機MemList是否有可執行工作, 若符合執行的job則排入執行工作
     */
    queryJobLikeList(ReqBody: DPB0058Req): Observable<ResDPB0058> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryJobLikeList),
            ReqBody: ReqBody
        } as ReqDPB0058
        const path = `${this.basePath}/DPB0058`;
        return this.api.npPost<ResDPB0058>(path, body);
    }

    /**
     * DPB0059: 指定某一筆工作[執行]/[重做]
     */
    doJobByPk(ReqBody: DPB0059Req ,completeUrl:string ): Observable<any> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.doJobByPk),
            ReqBody: ReqBody
        } as ReqDPB0059
        const path = `${this.basePath}/DPB0059`;

        // return this.api.excutePost<ResDPB0059>(path, body,['1491'],completeUrl);
        let token = this.tool.getToken();
            let signCode = this.api.cryptSignCode(body);
        return ajax({
            url :completeUrl ,
            method:'POST',
            headers:{
                'Content-Type': 'application/json',
                'SignCode': signCode,
                'Authorization': `Bearer ${token}`
            },
            body : body
        },)
    }

    /**
     * DPB0060: 未執行工作設為取消
     */
    cacelJobByPk(ReqBody: DPB0060Req): Observable<ResDPB0060> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.cacelJobByPk),
            ReqBody: ReqBody
        } as ReqDPB0060
        const path = `${this.basePath}/DPB0060`;
        return this.api.npPost<ResDPB0060>(path, body);
    }

    /**
     * DPB0061: 查看一筆工作明細
     */
    queryByPk(ReqBody: DPB0061Req): Observable<ResDPB0061> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.cacelJobByPk),
            ReqBody: ReqBody
        } as ReqDPB0061
        const path = `${this.basePath}/DPB0061`;
        return this.api.npPost<ResDPB0061>(path, body);
    }

    /**
     * DPB0062: 提供外部新增一筆 Job
     */
    createOneJob(ReqBody: DPB0062Req): Observable<ResDPB0062> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createOneJob),
            ReqBody: ReqBody
        } as ReqDPB0062
        const path = `${this.basePath}/DPB0062`;
        return this.api.npPost<ResDPB0062>(path, body);
    }
    queryRefeshMemListUrls():Observable<ResDPB0121>{
        const path = `${this.basePath}/DPB0121`;
        return this.api.excuteDpGet<ResDPB0121>(path,TxID.queryRefreshMemListUrls);
    }

    jobPost(path:string):Observable<any>{
        let header = this.api.getReqHeader(TxID.createOneJob);
        header.txID = 'refreshMemList';
        let body = {
            ReqHeader: header
        };
        return this.api.excutePost_bg('',body,undefined,path);
    }
}

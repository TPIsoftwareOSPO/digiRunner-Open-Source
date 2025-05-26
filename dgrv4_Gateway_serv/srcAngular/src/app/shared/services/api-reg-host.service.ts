import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import { AA0806Req, ReqAA0806, ResAA0806 } from 'src/app/models/api/RegHostService/aa0806.interface';
import { AA0801Req, ReqAA0801, ResAA0801, ResAA0801Before } from 'src/app/models/api/RegHostService/aa0801.interface';
import { AA0807Req, ReqAA0807, ResAA0807 } from 'src/app/models/api/RegHostService/aa0807.interface';
import { AA0803Req, ReqAA0803, ResAA0803, ResAA0803Before } from 'src/app/models/api/RegHostService/aa0803.interface';
import { AA0804Req, ReqAA0804, ResAA0804 } from 'src/app/models/api/RegHostService/aa0804.interface';
import { environment } from 'src/environments/environment';
import { ToolService } from './tool.service';
import * as shajs from 'sha.js';

@Injectable()
export class RegHostService {

    public get npBasePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService,
        private tool: ToolService
    ) { }

    /**
     * before
     * AA0801: 新增註冊主機
     * @param ReqBody
     */
    public addRegHost_before(): Observable<ResAA0801Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addRegHost),
            ReqBody: {}
        } as ReqAA0801
        const path = `${this.npBasePath}/AA0801?before`;
        return this.api.npPost<ResAA0801Before>(path, body);
    }

    /**
     * AA0801: 新增註冊主機
     * @param ReqBody
     */
    public addRegHost(ReqBody: AA0801Req): Observable<ResAA0801> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addRegHost),
            ReqBody: ReqBody
        } as ReqAA0801
        const path = `${this.npBasePath}/AA0801`;
        return this.api.npPost<ResAA0801>(path, body);
    }

    /**
     * before
     * AA0803: 更新註冊主機
     * @param ReqBody
     */
    public updateRegHost_before(): Observable<ResAA0803Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRegHost),
            ReqBody: {}
        } as ReqAA0803
        const path = `${this.npBasePath}/AA0803?before`;
        return this.api.npPost<ResAA0803Before>(path, body);
    }

    /**
     * AA0803: 更新註冊主機
     * @param ReqBody
     */
    public updateRegHost(ReqBody: AA0803Req): Observable<ResAA0803> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRegHost),
            ReqBody: ReqBody
        } as ReqAA0803
        const path = `${this.npBasePath}/AA0803`;
        return this.api.npPost<ResAA0803>(path, body);
    }

    /**
     * AA0804: 刪除註冊主機
     * @param ReqBody
     */
    public deleteRegHost(ReqBody: AA0804Req): Observable<ResAA0804> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteRegHost),
            ReqBody: ReqBody
        } as ReqAA0804
        const path = `${this.npBasePath}/AA0804`;
        return this.api.npPost<ResAA0804>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0806: 查詢註冊主機清單
     */
    queryRegHostList_1_ignore1298(ReqBody: AA0806Req): Observable<ResAA0806> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRegHostList_1),
            ReqBody: ReqBody
        } as ReqAA0806
        const path = `${this.npBasePath}/AA0806`;
        return this.api.excuteNpPost_ignore1298<ResAA0806>(path, body);
    }

    /**
     * AA0806: 查詢註冊主機清單
     */
    queryRegHostList_1(ReqBody: AA0806Req): Observable<ResAA0806> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRegHostList_1),
            ReqBody: ReqBody
        } as ReqAA0806
        const path = `${this.npBasePath}/AA0806`;
        return this.api.npPost<ResAA0806>(path, body);
    }

    /**
     * AA0807: 使用主機ID查詢註冊主機
     */
    queryRegHostByRegHostID(ReqBody: AA0807Req): Observable<ResAA0807> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRegHostByRegHostID),
            ReqBody: ReqBody
        } as ReqAA0807
        const path = `${this.npBasePath}/AA0807`;
        return this.api.npPost<ResAA0807>(path, body);
    }

}

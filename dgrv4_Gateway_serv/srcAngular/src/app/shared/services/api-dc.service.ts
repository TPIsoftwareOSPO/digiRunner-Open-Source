import { environment } from 'src/environments/environment';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { TxID } from 'src/app/models/common.enum';
import { ResAA0411, ReqAA0411, AA0411Req, ResAA0411Before } from 'src/app/models/api/DCService/aa0411.interface';
import { ResAA0413, ReqAA0413, AA0413Req } from 'src/app/models/api/DCService/aa0413.interface';
import { ResAA0414, ReqAA0414, AA0414Req } from 'src/app/models/api/DCService/aa0414.interface';
import { ResAA0415, ReqAA0415, AA0415Req } from 'src/app/models/api/DCService/aa0415.interface';
import { AA0417Req, ReqAA0417, ResAA0417 } from 'src/app/models/api/DCService/aa0417.interface';
import { AA0422Req, ReqAA0422, ResAA0422 } from 'src/app/models/api/DCService/aa0422.interface';

@Injectable()
export class DCService {

    public get npBasePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) { }

    /**
     * before
     * AA0411: 建立部屬容器
     * @param ReqBody
     */
    addDc_before(): Observable<ResAA0411Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addDC),
            ReqBody: {}
        } as ReqAA0411
        const path = `${this.npBasePath}/AA0411?before`;
        return this.api.npPost<ResAA0411Before>(path, body);
    }

    /**
     * AA0411: 建立部屬容器
     * @param ReqBody
     */
    public addDC(ReqBody: AA0411Req): Observable<ResAA0411> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addDC),
            ReqBody: ReqBody
        } as ReqAA0411
        const path = `${this.npBasePath}/AA0411`;
        return this.api.npPost<ResAA0411>(path, body);
    }

    /**
     * before
     * AA0413: 更新部屬容器
     * @param ReqBody
     */
    // updateDC_before(): Observable<ResAA0413> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.updateDC),
    //         ReqBody: {}
    //     } as ReqAA0413
    //     const path = `${this.npBasePath}/AA0413?before`;
    //     return this.api.npPost<ResAA0413>(path, body);
    // }

    /**
     * AA0413: 更新部屬容器
     * @param ReqBody
     */
    // public updateDC(ReqBody: AA0413Req): Observable<ResAA0413> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.updateDC),
    //         ReqBody: ReqBody
    //     } as ReqAA0413
    //     const path = `${this.npBasePath}/AA0413`;
    //     return this.api.npPost<ResAA0413>(path, body);
    // }

    /**
     * AA0414: 啟動/停止DC
     * @param ReqBody
     */
    // public startStopDC(ReqBody: AA0414Req): Observable<ResAA0414> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.startStopDC),
    //         ReqBody: ReqBody
    //     } as ReqAA0414
    //     const path = `${this.npBasePath}/AA0414`;
    //     return this.api.npPost<ResAA0414>(path, body);
    // }

    /**
     * AA0415: 刪除部屬容器
     * @param ReqBody
     */
    // public deleteDC(ReqBody: AA0415Req): Observable<ResAA0415> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.deleteDC),
    //         ReqBody: ReqBody
    //     } as ReqAA0415
    //     const path = `${this.npBasePath}/AA0415`;
    //     return this.api.npPost<ResAA0415>(path, body);
    // }

    /**
     * AA0416: 查詢部屬容器詳細
     * @param ReqBody
     */
    // queryDCByPk(ReqBody: AA0416Req): Observable<ResAA0416> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.queryDCByPk),
    //         ReqBody: ReqBody
    //     } as ReqAA0416
    //     const path = `${this.npBasePath}/AA0416`;
    //     return this.api.npPost<ResAA0416>(path, body);
    // }

    /**
     * AA0417: 查詢健康的節點清單
     * @param ReqBody
     */
    queryGreenTsmpNodeList(ReqBody: AA0417Req): Observable<ResAA0417> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryGreenTsmpNodeList),
            ReqBody: ReqBody
        } as ReqAA0417
        const path = `${this.npBasePath}/AA0417`;
        return this.api.npPost<ResAA0417>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0418: 查詢部屬容器清單
     */
    // queryDCList_1_ignore1298(ReqBody: AA0418Req): Observable<ResAA0418> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.queryDCList_1),
    //         ReqBody: ReqBody
    //     } as ReqAA0418
    //     const path = `${this.npBasePath}/AA0418`;
    //     return this.api.excuteNpPost_ignore1298<ResAA0418>(path, body);
    // }

    /**
     * AA0418: 查詢部屬容器清單
     */
    // queryDCList_1(ReqBody: AA0418Req): Observable<ResAA0418> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.queryDCList_1),
    //         ReqBody: ReqBody
    //     } as ReqAA0418
    //     const path = `${this.npBasePath}/AA0418`;
    //     return this.api.npPost<ResAA0418>(path, body);
    // }

    /**
     * AA0422: API測試區 - 查詢部署容器清單
     */
    queryDCList_2(ReqBody: AA0422Req): Observable<ResAA0422> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryDCList_2),
            ReqBody: ReqBody
        } as ReqAA0422
        const path = `${this.npBasePath}/AA0422`;
        return this.api.npPost<ResAA0422>(path, body);
    }

}


import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { DPB0102Req, RespDPB0102, ReqDPB0102 } from 'src/app/models/api/CycleScheduleService/dpb0102.interface';
import { DPB0101Req, RespDPB0101, ReqDPB0101, RespDPB0101Before } from 'src/app/models/api/CycleScheduleService/dpb0101.interface';
import { DPB0104Req, RespDPB0104, ReqDPB0104 } from 'src/app/models/api/CycleScheduleService/dpb0104.interface';
import { DPB0103Req, RespDPB0103, ReqDPB0103 } from 'src/app/models/api/CycleScheduleService/dpb0103.interface';
import { DPB0105Req, RespDPB0105, ReqDPB0105, RespDPB0105Before } from 'src/app/models/api/CycleScheduleService/dpb0105.interface';

@Injectable({
    providedIn: 'root'
})
export class CycleScheduleService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     *
     * @param ReqBody
     */
    createRjob_before(): Observable<RespDPB0101Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createRjob),
            ReqBody: {}
        } as ReqDPB0101;
        const path = `${this.basePath}/DPB0101?before`;
        return this.api.npPost<RespDPB0101Before>(path, body);
    }

    /**
     *
     * @param ReqBody
     * 20211008新增
     */
     createRjob_before2(): Observable<RespDPB0101Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createRjob),
            ReqBody: {}
        } as ReqDPB0101;
        const path = `${this.basePath}/DPB0101?before2`;
        return this.api.npPost<RespDPB0101Before>(path, body);
    }

    /**
     * DPB0101: 新增週期排程
     * 設定週期排程執行工作
     * @param ReqBody
     */
    createRjob(ReqBody: DPB0101Req): Observable<RespDPB0101> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createRjob),
            ReqBody: ReqBody
        } as ReqDPB0101;
        const path = `${this.basePath}/DPB0101`;
        return this.api.npPost<RespDPB0101>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0102: 查詢週期排程清單
     * 提供[關鍵字](UID、名稱、頻率、備註、大分類、子項目)、[狀態](全部、作廢、啟動、暫停、執行中)搜尋週期排程設定檔
     * 搜尋結果依照以下欄位順序排序: 下次執行時間 asc、名稱 asc、UID asc
     */
    queryRjobList_ignore1298(ReqBody: DPB0102Req): Observable<RespDPB0102> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRjobList),
            ReqBody: ReqBody
        } as ReqDPB0102;
        const path = `${this.basePath}/DPB0102`;
        return this.api.excuteNpPost_ignore1298<RespDPB0102>(path, body);
    }

    /**
     * DPB0102: 查詢週期排程清單
     * 提供[關鍵字](UID、名稱、頻率、備註、大分類、子項目)、[狀態](全部、作廢、啟動、暫停、執行中)搜尋週期排程設定檔
     * 搜尋結果依照以下欄位順序排序: 下次執行時間 asc、名稱 asc、UID asc
     */
    queryRjobList(ReqBody: DPB0102Req): Observable<RespDPB0102> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRjobList),
            ReqBody: ReqBody
        } as ReqDPB0102;
        const path = `${this.basePath}/DPB0102`;
        return this.api.npPost<RespDPB0102>(path, body);
    }

    /**
     * DPB0103: 依PK查詢週期排程明細
     * 依照週期排程UID查出設定檔及工作項目內容
     * @param ReqBody
     */
    queryRjobByPk(ReqBody: DPB0103Req): Observable<RespDPB0103> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRjobByPk),
            ReqBody: ReqBody
        } as ReqDPB0103;
        const path = `${this.basePath}/DPB0103`;
        return this.api.npPost<RespDPB0103>(path, body);
    }

    /**
     * DPB0104: 查詢週期排程執行歷程
     * 依照週期排程UID查出所有工作項目執行的歷程
     */
    queryRjobHistory(ReqBody: DPB0104Req): Observable<RespDPB0104> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRjobHistory),
            ReqBody: ReqBody
        } as ReqDPB0104;
        const path = `${this.basePath}/DPB0104`;
        return this.api.npPost<RespDPB0104>(path, body);
    }

    /**
     * DPB0105: 更新週期排程
     *
     */
    updateRjob(ReqBody: DPB0105Req): Observable<RespDPB0105> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRjob),
            ReqBody: ReqBody
        } as ReqDPB0105;
        const path = `${this.basePath}/DPB0105`;
        return this.api.npPost<RespDPB0105>(path, body);
    }

    updateRjob_before2(): Observable<RespDPB0105Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRjob),
            ReqBody: {}
        } as ReqDPB0105;
        const path = `${this.basePath}/DPB0105?before2`;
        return this.api.npPost<RespDPB0105Before>(path, body);
    }

}

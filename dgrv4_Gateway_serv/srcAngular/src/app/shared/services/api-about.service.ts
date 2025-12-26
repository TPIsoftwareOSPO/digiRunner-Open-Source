
import { Observable, Subject } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { ResDPB0032, ReqDPB0032 } from 'src/app/models/api/AboutService/dpb0032.interface';
import { TxID } from 'src/app/models/common.enum';
import { DPB0031Req, ResDPB0031, ReqDPB0031 } from 'src/app/models/api/AboutService/dpb0031.interface';
import { DPB0118Resp, ResDPB0118 } from 'src/app/models/api/AboutService/dpb0118.interface';

@Injectable({
    providedIn: 'root'
})
export class AboutService {


    moduleVersionSubject:Subject<DPB0118Resp> = new Subject();
    setModuleVersionData(value:any){
      this.moduleVersionSubject.next(value);
    }

    getModuleVersionData() {
      return this.moduleVersionSubject.asObservable();
    }

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
     * DPB0032: 關於網站查詢
     * 查詢一筆資料 , 該Table只會有一筆資料
     */
    queryAbout_0_ignore1298(): Observable<ResDPB0032> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAbout_0),
            ReqBody: {}
        } as ReqDPB0032
        const path = `${this.basePath}/DPB0032`;
        return this.api.excuteNpPost_ignore1298<ResDPB0032>(path, body);
    }

    /**
     * DPB0032: 關於網站查詢
     * 查詢一筆資料 , 該Table只會有一筆資料
     */
    queryAbout_0(): Observable<ResDPB0032> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAbout_0),
            ReqBody: {}
        } as ReqDPB0032
        const path = `${this.basePath}/DPB0032`;
        return this.api.npPost<ResDPB0032>(path, body);
    }

    /**
     * DPB0031: 關於網站save(CU)
     * 對於一筆資料的create或是update, 該Table只會有一筆資料
     */
    saveAbout(ReqBody: DPB0031Req): Observable<ResDPB0031> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.saveAbout),
            ReqBody: ReqBody
        } as ReqDPB0031
        const path = `${this.basePath}/DPB0031`;
        return this.api.npPost<ResDPB0031>(path, body);
    }

    /**
     * DPB0118: 查詢版本資訊
     * 顯示各模組版本資料
     */
    queryModuleVersion(): Observable<ResDPB0118> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleVersion),
            ReqBody: {}
        } as ReqDPB0031
        const path = `${this.basePath}/DPB0118`;
        return this.api.npPost<ResDPB0118>(path, body);
    }

}

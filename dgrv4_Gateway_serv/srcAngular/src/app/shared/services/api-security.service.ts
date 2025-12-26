
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { AA1116Req, ReqAA1116, ResAA1116 } from 'src/app/models/api/SecurityService/aa1116.interface';
import { AA1101Req, ReqAA1101, ResAA1101, ResAA1101Before } from 'src/app/models/api/SecurityService/aa1101.interface';
import { AA1103Req, ReqAA1103, ResAA1103, ResAA1103Before } from 'src/app/models/api/SecurityService/aa1103.interface';
import { AA1105Req, ReqAA1105, ResAA1105 } from 'src/app/models/api/SecurityService/aa1105.interface';
import { AA1104Req, ReqAA1104, ResAA1104 } from 'src/app/models/api/SecurityService/aa1104.interface';

@Injectable({
    providedIn: 'root'
})
export class SecurityService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * before
     * AA1101: 新增安全等級
     * @param ReqBody
     */
    addSecurityLevel_before(): Observable<ResAA1101Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addSecurityLevel),
            ReqBody: {}
        } as ReqAA1101
        const path = `${this.basePath}/AA1101?before`;
        return this.api.npPost<ResAA1101Before>(path, body);
    }

    /**
     * AA1101: 新增安全等級
     * @param ReqBody
     */
    addSecurityLevel(ReqBody: AA1101Req): Observable<ResAA1101> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addSecurityLevel),
            ReqBody: ReqBody
        } as ReqAA1101
        const path = `${this.basePath}/AA1101`;
        return this.api.npPost<ResAA1101>(path, body);
    }

    /**
     * before
     * AA1103: 更新安全等級
     * 依照 安全等級ID 及 安全等級名稱 更新一筆安全等級資料
     * @param ReqBody
     */
    updateSecurityLevel_before(): Observable<ResAA1103Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateSecurityLevel),
            ReqBody: {}
        } as ReqAA1103
        const path = `${this.basePath}/AA1103?before`;
        return this.api.npPost<ResAA1103Before>(path, body);
    }

    /**
     * AA1103: 更新安全等級
     * 依照 安全等級ID 及 安全等級名稱 更新一筆安全等級資料
     * @param ReqBody
     */
    updateSecurityLevel(ReqBody: AA1103Req): Observable<ResAA1103> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateSecurityLevel),
            ReqBody: ReqBody
        } as ReqAA1103
        const path = `${this.basePath}/AA1103`;
        return this.api.npPost<ResAA1103>(path, body);
    }

    /**
     * AA1104: 刪除安全等級
     * 依照 安全等級ID 及 安全等級名稱 刪除一筆安全等級資料
     * @param ReqBody
     */
    deleteSecurityLevel(ReqBody: AA1104Req): Observable<ResAA1104> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteSecurityLevel),
            ReqBody: ReqBody
        } as ReqAA1104
        const path = `${this.basePath}/AA1104`;
        return this.api.npPost<ResAA1104>(path, body);
    }

    /**
     * AA1105: 更新安全等級
     * 依照 安全等級ID 及 安全等級名稱 更新一筆安全等級資料
     * @param ReqBody
     */
    querySecurityLevelDetail(ReqBody: AA1105Req): Observable<ResAA1105> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySecurityLevelDetail),
            ReqBody: ReqBody
        } as ReqAA1105
        const path = `${this.basePath}/AA1105`;
        return this.api.npPost<ResAA1105>(path, body);
    }

    /**
     * 忽略查無資料
     * AA1116: 查詢安全等級清單(原aa1102)
     * 提供關鍵字(安全等級ID、安全等級名稱、安全等級描述)搜尋安全等級清單。
     * @param body ReqAA1116
     */
    querySecurityLevelList_ignore1298(ReqBody: AA1116Req): Observable<ResAA1116> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySecurityLevelList),
            ReqBody: ReqBody
        } as ReqAA1116
        const path = `${this.basePath}/AA1116`;
        return this.api.excuteNpPost_ignore1298<ResAA1116>(path, body);
    }

    /**
     * AA1116: 查詢安全等級清單(原aa1102)
     * 提供關鍵字(安全等級ID、安全等級名稱、安全等級描述)搜尋安全等級清單。
     * @param body ReqAA1116
     */
    querySecurityLevelList(ReqBody: AA1116Req): Observable<ResAA1116> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySecurityLevelList),
            ReqBody: ReqBody
        } as ReqAA1116
        const path = `${this.basePath}/AA1116`;
        return this.api.npPost<ResAA1116>(path, body);
    }

}

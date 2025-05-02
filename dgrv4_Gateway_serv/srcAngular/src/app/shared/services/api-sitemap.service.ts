import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { ResDPF0011, ReqDPF0011 } from 'src/app/models/api/SiteMapService/dpf0011.interface';
import { TxID } from 'src/app/models/common.enum';
import { DPB0033Req, ResDPB0033, ReqDPB0033 } from 'src/app/models/api/SiteMapService/dpb0033.interface';
import { DPB0034Req, ResDPB0034, ReqDPB0034 } from 'src/app/models/api/SiteMapService/dpb0034.interface';
import { DPB0035Req, ResDPB0035, ReqDPB0035 } from 'src/app/models/api/SiteMapService/dpb0035.interface';

@Injectable({
    providedIn: 'root'
})
export class SitemapService {

    public get basePathFe(): string {
        return 'tsmpdpapi/11';
    }

    public get basePathBe(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * DPB0033: 網站地圖新增節點
     * 新增一筆節點資料, 必需傳入父節點Id
     * @param Req_DP0701
     */
    addNode(ReqBody: DPB0033Req): Observable<ResDPB0033> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addNode),
            ReqBody: ReqBody
        } as ReqDPB0033
        const path = `${this.basePathBe}/DPB0033`;
        return this.api.npPost<ResDPB0033>(path, body);
    }

    /**
     * DPB00341: 網站地圖更新節點
     * 更新一筆資料, by Id
     */
    updateNodeById(ReqBody: DPB0034Req): Observable<ResDPB0034> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateNodeById),
            ReqBody: ReqBody
        } as ReqDPB0034
        const path = `${this.basePathBe}/DPB0034`;
        return this.api.npPost<ResDPB0034>(path, body);
    }

    /**
     * DPB0035: 網站地圖刪除節點
     * delete一筆資料, by Id
     */
    deleteNodeById(ReqBody: DPB0035Req): Observable<ResDPB0035> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteNodeById),
            ReqBody: ReqBody
        } as ReqDPB0035
        const path = `${this.basePathBe}/DPB0035`;
        return this.api.npPost<ResDPB0035>(path, body);
    }

    /**
     * 忽略查無資料
     * DPF0011: 查單一頁面 (網站地圖)
     * 沒有查詢參數, 查詢出tree 結構, 資料需要orderby SITE_PARENT_ID,DATA_SORT
     */
    querySiteMap_ignore298(): Observable<ResDPF0011> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySiteMap),
            ReqBody: {}
        } as ReqDPF0011
        const path = `${this.basePathFe}/DPF0011`;
        return this.api.excuteNpPost_ignore1298<ResDPF0011>(path, body);
    }

    /**
     * DPF0011: 查單一頁面 (網站地圖)
     * 沒有查詢參數, 查詢出tree 結構, 資料需要orderby SITE_PARENT_ID,DATA_SORT
     */
    querySiteMap(): Observable<ResDPF0011> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySiteMap),
            ReqBody: {}
        } as ReqDPF0011
        const path = `${this.basePathFe}/DPF0011`;
        return this.api.npPost<ResDPF0011>(path, body);
    }

}

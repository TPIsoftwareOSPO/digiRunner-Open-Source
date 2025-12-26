import { environment } from 'src/environments/environment';
import { ReqNAA0412 } from './../../models/api/ModuleService/naa0412.interface';
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import * as model from '../../models/api/ModuleService';
import { TxID } from 'src/app/models/common.enum';
import { ToolService } from './tool.service';
import { AA0403Req } from 'src/app/models/api/ModuleService/aa0403.interface';
import { AA0404Req } from 'src/app/models/api/ModuleService/aa0404.interface';
import { AA0405Req } from 'src/app/models/api/ModuleService/aa0405.interface';
import { Req_n0402 } from 'src/app/models/api/ModuleService/naa0402.interface';
import { Req_n0403 } from 'src/app/models/api/ModuleService/naa0403.interface';
import { Req_n0404 } from 'src/app/models/api/ModuleService/naa0404.interface';
import { Req_n0405 } from 'src/app/models/api/ModuleService/naa0405.interface';
import { Req_0411 } from 'src/app/models/api/ModuleService/naa0411.interface';
import { Req_0412 } from 'src/app/models/api/ModuleService/naa0412.interface';
import { Req_0415 } from 'src/app/models/api/ModuleService/naa0415.interface';
import { Req_0414 } from 'src/app/models/api/ModuleService/naa0414.interface';
import { Req_0413 } from 'src/app/models/api/ModuleService/naa0413.interface';
import { AA0419Req, ReqAA0419, ResAA0419 } from 'src/app/models/api/ModuleService/aa0419.interface';
import { AA0420Req, ReqAA0420, ResAA0420 } from 'src/app/models/api/ModuleService/aa0420.interface';
import { AA0421Req, ReqAA0421, ResAA0421 } from 'src/app/models/api/ModuleService/aa0421.interface';

@Injectable()
export class ModuleService {

    uploadModuleUrl: string;
    uploadNetModuleUrl: string;

    private get npBasePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    private get baseNetPath(): string {
        return 'tsmpc/tsmpnaa';
    }

    private get basedotNetPath(): string {
        return 'tsmpc/nev.tsmpnaa/tsmpnaa';
    }
    private get testDotNetPath(): string {
        return 'tsmpnaa/04';
    }

    constructor(private api: ApiBaseService, private tool: ToolService) {
        this.uploadModuleUrl = `${this.api.baseUrl}/${this.npBasePath}/AA0401`;
        this.uploadNetModuleUrl = `${this.api.baseUrl}/${this.basedotNetPath}/NAA0401`;
        // this.uploadNetModuleUrl = `${this.api.netBaseUrl}/${this.testDotNetPath}/naa0401`;
    }

    /**
     * AA0403: 查詢ModuleDetail
     * 查詢TSMP部署的Module詳細內容。
     * @param body ReqAA0403
     */
    queryModuleDetail(ReqBody: AA0403Req): Observable<model.ResAA0403> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleDetail),
            ReqBody: ReqBody
        } as model.ReqAA0403
        const path = `${this.npBasePath}/AA0403`;
        return this.api.npPost<model.ResAA0403>(path, body);
    }

    /**
     * AA0404: 啟動/停止Module
     * 啟動/停止TSMP部署的Module。
     * @param body ReqAA0404
     */
    startStopModule(ReqBody: AA0404Req): Observable<model.ResAA0404> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.startStopModule),
            ReqBody: ReqBody
        } as model.ReqAA0404
        const path = `${this.npBasePath}/AA0404`;
        return this.api.npPost<model.ResAA0404>(path, body);
    }
    /**
     * AA0405: 刪除Module
     * 刪除在TSMP部署的Module。只有Active狀態的Module不可被刪除。
     * @param body ReqAA0405
     */
    deleteModule(ReqBody: AA0405Req): Observable<model.ResAA0405> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteModule),
            ReqBody: ReqBody
        } as model.ReqAA0405
        const path = `${this.npBasePath}/AA0405`;
        return this.api.npPost<model.ResAA0405>(path, body);
    }

    /**
     * NAA0402: 查詢 ModuleList
     * 查詢 TSMPN 部署的 Module清單。TSMPN 要部署的 API 包在 Module 中，Module 檔為zip。
     * 預設查詢 TSMP 中部署的所有 Module 與所有版本。
     * Module有兩個狀態:
     * 0: Inactive, 此時該 Module 中的所有 API 無法被呼叫。
     * 1: Active, 此時 Module 中的 API 可被呼叫。
     * @param req_0402
     */
    public queryNetModuleList(req_n0402: Req_n0402): Observable<model.ResNAA0402> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNetModuleList),
            Req_0402: req_n0402
        } as model.ReqNAA0402
        const path = `${this.basedotNetPath}/NAA0402`;
        // const path = `${this.testDotNetPath}/naa0402`;
        return this.api.dotNetPost<model.ResNAA0402>(path, body);
    }
    /**
     * NAA0403: 查詢 ModuleDetail
     * 查詢 TSMPN 部署的 Module 詳細內容。
     * @param req_0403
     */
    public queryNetModuleDetail(req_n0403: Req_n0403): Observable<model.ResNAA0403> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryNetModuleDetail),
            Req_0403: req_n0403
        } as model.ReqNAA0403
        const path = `${this.basedotNetPath}/NAA0403`;
        // const path = `${this.testDotNetPath}/naa0403`;
        return this.api.dotNetPost<model.ResNAA0403>(path, body);
    }
    /**
     * NAA0404: Bind/Unbind Module
     * Bind/Unbind TSMPN 部署的 Module。
     * @param req_0404
     */
    public bindModule(req_n0404: Req_n0404): Observable<model.ResNAA0404> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.bindModule),
            Req_0404: req_n0404
        } as model.ReqNAA0404
        const path = `${this.basedotNetPath}/NAA0404`;
        // const path = `${this.testDotNetPath}/naa0404`;
        return this.api.dotNetPost<model.ResNAA0404>(path, body);
    }
    /**
     * NAA0405: 刪除 Module
     * 刪除在 TSMPN 部署的 Module。TSMPN_SITE_MODULE 有參考的 Module 不可被刪除。
     * @param req_0405
     */
    public deleteNetModules(req_n0405: Req_n0405): Observable<model.ResNAA0405> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteNetModule),
            Req_0405: req_n0405
        } as model.ReqNAA0405
        const path = `${this.basedotNetPath}/NAA0405`;
        // const path = `${this.testDotNetPath}/naa0405`;
        return this.api.dotNetPost<model.ResNAA0405>(path, body);
    }
    /**
     * NAA0411: 新增 Site
     * "新增一個 Site (IIS 站台)。
     * 一個 Site 僅部署一個 Modules。"。
     * @param req_0411
     */
    public addSite(req_0411: Req_0411): Observable<model.ResNAA0411> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addSite),
            Req_0411: req_0411
        } as model.ReqNAA0411
        const path = `${this.basedotNetPath}/NAA0411`;
        // const path = `${this.testDotNetPath}/naa0411`;
        return this.api.dotNetPost<model.ResNAA0411>(path, body);
    }
    /**
     * NAA0412: 查詢 SiteList
     * 查詢 Site (IIS 站台)清單。
     * 預設查全部。
     * @param req_0412
     */
    public querySiteList(req_0412: Req_0412): Observable<model.ResNAA0412> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.querySiteList),
            Req_0412: this.formateReq0412(req_0412),
        } as ReqNAA0412
        const path = `${this.basedotNetPath}/NAA0412`;
        // const path = `${this.testDotNetPath}/naa0412`;
        return this.api.dotNetPost<model.ResNAA0412>(path, body);
    }

    formateReq0412(req_0412: Req_0412){
      for (var key in req_0412) {
        if (req_0412.hasOwnProperty(key)) {
          if (req_0412[key] === null || req_0412[key] == undefined || req_0412[key] === '')
          {
            delete req_0412[key];
          }
        }
      }
      return req_0412;
    }

    /**
     * NAA0413: 更新 Site
     * 更新 Site (IIS 站台)。
     * @param req_0413
     */
    // public updateSite(req_0413: Req_0413): Observable<model.ResNAA0413> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.updateSite),
    //         Req_0413: req_0413
    //     } as model.ReqNAA0413
    //     const path = `${this.basedotNetPath}/NAA0413`;
    //     // const path = `${this.testDotNetPath}/naa0413`;
    //     return this.api.dotNetPost<model.ResNAA0413>(path, body);
    // }
    /**
     * NAA0414: 啟動/停止 Site
     * 啟動/停止 TSMPN 配置的 Site。
     * @param req_0414
     */
    public startStopSite(req_0414: Req_0414): Observable<model.ResNAA0414> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.startStopSite),
            Req_0414: req_0414
        } as model.ReqNAA0414
        const path = `${this.basedotNetPath}/NAA0414`;
        // const path = `${this.testDotNetPath}/naa0414`;
        return this.api.dotNetPost<model.ResNAA0414>(path, body);
    }
    /**
     * NAA0412: 查詢 SiteList
     * 查詢 Site (IIS 站台)清單。
     * 預設查全部。
     * @param req_0415
     */
    // public deleteSite(req_0415: Req_0415): Observable<model.ResNAA0415> {
    //     let body = {
    //         ReqHeader: this.api.getReqHeader(TxID.deleteSite),
    //         Req_0415: req_0415
    //     } as model.ReqNAA0415
    //     const path = `${this.basedotNetPath}/NAA0415`;
    //     // const path = `${this.testDotNetPath}/naa0415`;
    //     return this.api.dotNetPost<model.ResNAA0415>(path, body);
    // }

    /**
     * 忽略查無資料
     * AA0419: 查詢模組清單
     */
    queryModuleList_1_ignore1298(ReqBody: AA0419Req): Observable<ResAA0419> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleList_1),
            ReqBody: ReqBody
        } as ReqAA0419
        const path = `${this.npBasePath}/AA0419`;
        return this.api.excuteNpPost_ignore1298<ResAA0419>(path, body);
    }

    /**
     * AA0419: 查詢模組清單
     */
    queryModuleList_1(ReqBody: AA0419Req): Observable<ResAA0419> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleList_1),
            ReqBody: ReqBody
        } as ReqAA0419
        const path = `${this.npBasePath}/AA0419`;
        return this.api.npPost<ResAA0419>(path, body);
    }

    /**
     * AA0420: 查詢模組版本清單
     */
    queryModuleVerList(ReqBody: AA0420Req): Observable<ResAA0420> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryModuleVerList),
            ReqBody: ReqBody
        } as ReqAA0420
        const path = `${this.npBasePath}/AA0420`;
        return this.api.npPost<ResAA0420>(path, body);
    }

    /**
     * AA0421: 依模組版本查詢API清單
     */
    queryAPIByModVer(ReqBody: AA0421Req): Observable<ResAA0421> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryAPIByModVer),
            ReqBody: ReqBody
        } as ReqAA0421
        const path = `${this.npBasePath}/AA0421`;
        return this.api.npPost<ResAA0421>(path, body);
    }

}

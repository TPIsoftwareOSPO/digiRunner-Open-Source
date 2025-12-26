
import { Observable } from 'rxjs';
import { Injectable } from "@angular/core";
import { ApiBaseService } from './api-base.service';
import { environment } from 'src/environments/environment';
import { TxID } from 'src/app/models/common.enum';
import { AA0020Req, ResAA0020, ReqAA0020 } from 'src/app/models/api/RoleService/aa0020.interface';
import { AA0022Req, ResAA0022, ReqAA0022 } from 'src/app/models/api/RoleService/aa0022.interface';
import { AA0011Req, ResAA0011, ReqAA0011 } from 'src/app/models/api/UserService/aa0011.interface';
import { AA0013Req, ResAA0013, ReqAA0013 } from 'src/app/models/api/UserService/aa0013.interface';
import { AA0014Req, ResAA0014, ReqAA0014 } from 'src/app/models/api/UserService/aa0014.interface';
import { DPB0115Req, RespDPB0115, ReqDPB0115 } from 'src/app/models/api/RoleService/dpb0115.interface';
import { DPB0110Req, RespDPB0110, ReqDPB0110, ResDPB0110Before } from 'src/app/models/api/RoleService/dpb0110.interface';
import { DPB0111Req, RespDPB0111, ReqDPB0111 } from 'src/app/models/api/RoleService/dpb0111.interface';
import { DPB0112Req, RespDPB0112, ReqDPB0112 } from 'src/app/models/api/RoleService/dpb0112.interface';
import { DPB0113Req, RespDPB0113, ReqDPB0113, ResDPB0113Before } from 'src/app/models/api/RoleService/dpb0113.interface';
import { DPB0114Req, RespDPB0114, ReqDPB0114 } from 'src/app/models/api/RoleService/dpb0114.interface';
import { AA0016Req, ResAA0016, ReqAA0016 } from 'src/app/models/api/RoleService/aa0016.interface';
import { AA0017Req, ReqAA0017, ResAA0017 } from 'src/app/models/api/RoleService/aa0017.interface';
import { AA0021Req, ResAA0021, ReqAA0021 } from 'src/app/models/api/RoleService/aa0021.interface';
import { AA0018Req, ReqAA0018, ResAA0018 } from 'src/app/models/api/RoleService/aa0018.interface';
import { AA0023Req, ReqAA0023, ResAA0023 } from 'src/app/models/api/RoleService/aa0023.interface';

@Injectable({
    providedIn: 'root'
})
export class RoleService {

    public get basePath(): string {
      return environment.isv4 ? 'dgrv4/11' : 'tsmpdpaa/11';
    }

    constructor(
        private api: ApiBaseService
    ) {
        this.api.baseUrl = environment.dpPath;
    }

    /**
     * AA0011: 新增TRole
     * 在TSMP Admin中新增TSMP後台管理使用者角色，簡稱TRole。系統依使用者的TRole決定功能權限。
     * 新增成功時返回roleID。TSMP Admin API系統預設有"Manager" TRole，可以使用所有API。
     * @param body ReqAA0011
     */
    addTRole(ReqBody: AA0011Req): Observable<ResAA0011> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addTRole),
            ReqBody: ReqBody
        } as ReqAA0011
        const path = `${this.basePath}/AA0011`;
        return this.api.npPost<ResAA0011>(path, body);
    }

    /**
     * AA0013: 更新TRoleFunc
     * 在TSMP Admin中更新TSMP後台管理角色的功能權限。
     * @param body ReqAA0013
     */
    updateTRoleFunc(ReqBody: AA0013Req): Observable<ResAA0013> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTRoleFunc),
            ReqBody: ReqBody
        } as ReqAA0013
        const path = `${this.basePath}/AA0013`;
        return this.api.npPost<ResAA0013>(path, body);
    }

    /**
     * AA0014: 刪除TRole
     * 在TSMP Admin中刪除TSMP後台管理角色。刪除時要確認該Role中沒有任何User，
     * 也就是TSMP_USER_ROLE Table中沒有該Role的資料，然後一並將TSMP_ROLE_FUNC Table中該Role的資料刪除。
     * @param body ReqAA0014
     */
    deleteTRole(ReqBody: AA0014Req): Observable<ResAA0014> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteTRole),
            ReqBody: ReqBody
        } as ReqAA0014
        const path = `${this.basePath}/AA0014`;
        return this.api.npPost<ResAA0014>(path, body);
    }

    /**
     * AA0016: 新增Role-Role Mapping
     * 1. 在TSMP 中新增TSMP Role-Role角色Mapping。
     * 2. 僅Admin角色可呼叫。
     */
    addTRoleRoleMap(ReqBody: AA0016Req): Observable<ResAA0016> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.addTRoleRoleMap),
            ReqBody: ReqBody
        } as ReqAA0016;
        const path = `${this.basePath}/AA0016`;
        return this.api.npPost<ResAA0016>(path, body);
    }

    /**
     * AA0017: 刪除Role-Role Mapping
     * 1. Admin角色在TSMP_ROLE中刪除Role時（應先檢查該Role是否有其他系統使用者持有）一併將Role-Role角色Mapping刪除。
     * 2. 僅Admin角色可呼叫。
     */
    deleteTRoleRoleMap(ReqBody: AA0017Req): Observable<ResAA0017> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteTRoleRoleMap),
            ReqBody: ReqBody
        } as ReqAA0017;
        const path = `${this.basePath}/AA0017`;
        return this.api.npPost<ResAA0017>(path, body);
    }

    /**
     * AA0018: 更新登入角色的設定角色清單
     * @param ReqBody
     */
    updateTRoleRoleMap(ReqBody: AA0018Req): Observable<ResAA0018> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateTRoleRoleMap),
            ReqBody: ReqBody
        } as ReqAA0018;
        const path = `${this.basePath}/AA0018`;
        return this.api.npPost<ResAA0018>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0020: 查詢角色
     */
    queryTRoleList_v3_ignore1298(ReqBody: AA0020Req): Observable<ResAA0020> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTRoleList_v3),
            ReqBody: ReqBody
        } as ReqAA0020;
        const path = `${this.basePath}/AA0020`;
        return this.api.excuteNpPost_ignore1298<ResAA0020>(path, body);
    }

    /**
     * AA0020: 查詢角色
     */
    queryTRoleList_v3(ReqBody: AA0020Req): Observable<ResAA0020> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTRoleList_v3),
            ReqBody: ReqBody
        } as ReqAA0020;
        const path = `${this.basePath}/AA0020`;
        return this.api.npPost<ResAA0020>(path, body);
    }

    /**
     * AA0021:
     * @param ReqBody
     */
    queryTRoleRoleMapDetail(ReqBody: AA0021Req): Observable<ResAA0021> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTRoleRoleMapDetail),
            ReqBody: ReqBody
        } as ReqAA0021;
        const path = `${this.basePath}/AA0021`;
        return this.api.npPost<ResAA0021>(path, body);
    }

    /**
     * 忽略查無資料
     * AA0022: 取得登入角色可設定的角色清單
     */
    queryTRoleRoleMap_ignore1298(ReqBody: AA0022Req): Observable<ResAA0022> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTRoleList_v3),
            ReqBody: ReqBody
        } as ReqAA0022;
        const path = `${this.basePath}/AA0022`;
        return this.api.excuteNpPost_ignore1298<ResAA0022>(path, body);
    }

    /**
     * AA0022: 取得登入角色可設定的角色清單
     */
    queryTRoleRoleMap(ReqBody: AA0022Req): Observable<ResAA0022> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryTRoleList_v3),
            ReqBody: ReqBody
        } as ReqAA0022;
        const path = `${this.basePath}/AA0022`;
        return this.api.npPost<ResAA0022>(path, body);
    }

    /**
     * AA0023: 取得登入角色可以設定全部角色清單，不用以"角色"分組(原AA0015)
     */
    queryRoleRoleList(ReqBody: AA0023Req): Observable<ResAA0023> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRoleRoleList),
            ReqBody: ReqBody
        } as ReqAA0023;
        const path = `${this.basePath}/AA0023`;
        return this.api.npPost<ResAA0023>(path, body);
    }

    /**
     * before
     * DPB0110: 新增對應
     * 新增一筆角色ID與API交易代碼(txID)的對應關係。
     * @param ReqBody
     */
    createRTMap_before(): Observable<ResDPB0110Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createRTMap),
            ReqBody: {}
        } as ReqDPB0110;
        const path = `${this.basePath}/DPB0110?before`;
        return this.api.npPost<ResDPB0110Before>(path, body);
    }

    /**
     * DPB0110: 新增對應
     * 新增一筆角色ID與API交易代碼(txID)的對應關係。
     * @param ReqBody
     */
    createRTMap(ReqBody: DPB0110Req): Observable<RespDPB0110> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.createRTMap),
            ReqBody: ReqBody
        } as ReqDPB0110;
        const path = `${this.basePath}/DPB0110`;
        return this.api.npPost<RespDPB0110>(path, body);
    }

    /**
     * 忽略查無資料
     * DPB0111: 查詢對應清單
     * @param ReqBody
     */
    queryRTMapList_ignore1298(ReqBody: DPB0111Req): Observable<RespDPB0111> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRTMapList),
            ReqBody: ReqBody
        } as ReqDPB0111;
        const path = `${this.basePath}/DPB0111`;
        return this.api.excuteNpPost_ignore1298<RespDPB0111>(path, body);
    }

    /**
     * DPB0111: 查詢對應清單
     * @param ReqBody
     */
    queryRTMapList(ReqBody: DPB0111Req): Observable<RespDPB0111> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRTMapList),
            ReqBody: ReqBody
        } as ReqDPB0111;
        const path = `${this.basePath}/DPB0111`;
        return this.api.npPost<RespDPB0111>(path, body);
    }

    /**
     * DPB0112: 查詢對應byPk
     * @param ReqBody
     */
    queryRTMapByPk(ReqBody: DPB0112Req): Observable<RespDPB0112> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRTMapByPk),
            ReqBody: ReqBody
        } as ReqDPB0112;
        const path = `${this.basePath}/DPB0112`;
        return this.api.npPost<RespDPB0112>(path, body);
    }

    /**
     * before
     * DPB0113: 修改對應
     * @param ReqBody
     */
    updateRTMap_before(): Observable<ResDPB0113Before> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRTMap),
            ReqBody: {}
        } as ReqDPB0113;
        const path = `${this.basePath}/DPB0113?before`;
        return this.api.npPost<ResDPB0113Before>(path, body);
    }

    /**
     * DPB0113: 修改對應
     * @param ReqBody
     */
    updateRTMap(ReqBody: DPB0113Req): Observable<RespDPB0113> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.updateRTMap),
            ReqBody: ReqBody
        } as ReqDPB0113;
        const path = `${this.basePath}/DPB0113`;
        return this.api.npPost<RespDPB0113>(path, body);
    }

    /**
     * DPB0114: updateRTMap
     * @param ReqBody
     */
    deleteRTMap(ReqBody: DPB0114Req): Observable<RespDPB0114> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.deleteRTMap),
            ReqBody: ReqBody
        } as ReqDPB0114;
        const path = `${this.basePath}/DPB0114`;
        return this.api.npPost<RespDPB0114>(path, body);
    }

    /**
     * DPB0115: 查詢對應ByUK
     * 依照使用者名稱(或角色ID)及交易代碼查詢交易代碼是否可用
     * 後端檢核: 若依使用者名稱查詢不到所屬角色, 則 throw 1264
     */
    queryRTMapByUk(ReqBody: DPB0115Req): Observable<RespDPB0115> {
        let body = {
            ReqHeader: this.api.getReqHeader(TxID.queryRTMapByUk),
            ReqBody: ReqBody
        } as ReqDPB0115;
        const path = `${this.basePath}/DPB0115`;
        return this.api.npPost<RespDPB0115>(path, body);
    }

}

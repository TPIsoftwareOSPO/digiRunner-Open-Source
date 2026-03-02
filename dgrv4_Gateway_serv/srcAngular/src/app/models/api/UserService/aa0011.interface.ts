import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0011: 新增TRole
 * 在TSMP Admin中新增TSMP後台管理使用者角色，簡稱TRole。系統依使用者的TRole決定功能權限。
 * 新增成功時返回roleID。
 * TSMP Admin API系統預設有"Manager" TRole，可以使用所有API。
 */
export interface ReqAA0011 extends BaseReq {
    ReqBody: AA0011Req;
}
export interface AA0011Req {
    roleName: string;
    roleAlias: string;
    funcCodeList: string[];
}

export interface ResAA0011 extends BaseRes {
    RespBody: AA0011Resp;
}
export interface AA0011Resp {
    roleId: string;
}

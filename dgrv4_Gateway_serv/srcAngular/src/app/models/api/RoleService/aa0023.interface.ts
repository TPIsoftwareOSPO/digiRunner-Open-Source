import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA0023 extends BaseReq {
    ReqBody: AA0023Req;
}
export interface AA0023Req {
    roleId?: string;
    keyword?: string;
    paging?: string; // "Y" 或 NULL =要分頁; "N"=不用分頁
}

export interface ResAA0023 extends BaseRes {
    RespBody: AA0023Resp;
}
export interface AA0023Resp {
    roleRoleMappingList: Array<AA0023RoleInfo>;
}
export interface AA0023RoleInfo {
    roleId: string;
    roleName: string;
    roleAlias: string;
}
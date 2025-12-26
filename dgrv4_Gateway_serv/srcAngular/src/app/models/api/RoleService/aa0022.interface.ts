import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA0022 extends BaseReq {
    ReqBody: AA0022Req;
}
export interface AA0022Req {
    roleId?: string;
    keyword?: string;
}

export interface ResAA0022 extends BaseRes {
    RespBody: AA0022Resp;
}
export interface AA0022Resp {
    roleRoleMappingList: Array<AA0022List>;
}
export interface AA0022List {
    roleId: string;
    roleName: string;
    roleAlias: string;
    roleRoleMapping: Array<AA0022Detail>;
    roleRoleMappingInfo: string; // 完整的 可授權角色清單
    oriRoleRoleMappingInfo: string; // 截斷後的 可授權角色清單
    msgTruncated: boolean;
}
export interface AA0022Detail {
    roleId: string;
    roleName: string;
    roleAlias: string;
}
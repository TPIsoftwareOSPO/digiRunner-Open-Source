import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0021 extends BaseReq {
    ReqBody: AA0021Req;
}
export interface AA0021Req {
    roleName: string; // user role
}

export interface ResAA0021 extends BaseRes {
    RespBody: AA0021Resp;
}
export interface AA0021Resp {
    roleName: string;
    roleAlias: string;
    roleMappingInfo: Array<AA0021RoleInfo>;
}
export interface AA0021RoleInfo {
    roleName: string;
    roleAlias: string;
    roleAliasRoleName: string;
}
import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA0020 extends BaseReq {
    ReqBody: AA0020Req;
}
export interface AA0020Req {
    roleId?: string;
    keyword?: string;
    funcFlag: boolean; // 是否需要TSMP_ROLE_FUNC，前端固定傳true
    authorityFlag: boolean; // 是否查詢"登入角色"擁有的"可授權角色清單"(AA0018會使用到)，前端固定傳false
    roleName?: string; // 登入角色(AA0018會使用到)，若authorityFlag為true，才需要傳。
}

export interface ResAA0020 extends BaseRes {
    RespBody: AA0020Resp;
}
export interface AA0020Resp {
    roleDetailList: Array<AA0020List>;
}
export interface AA0020List {
    roleID: string;
    roleName: string;
    roleAlias: string;
    funcCodeList: Array<string>;
    mappingFlag: boolean;
}
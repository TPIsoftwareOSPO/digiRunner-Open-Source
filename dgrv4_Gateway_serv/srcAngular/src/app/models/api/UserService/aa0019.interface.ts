import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA0019 extends BaseReq {
    ReqBody: AA0019Req;
}
export interface AA0019Req {
    userId?: string;
    keyword?: string;
    roleName?: string;
    orgName?: string;
}

export interface ResAA0019 extends BaseRes {
    RespBody: AA0019Resp;
}
export interface AA0019Resp {
    userInfoList: Array<AA0019List>;
}
export interface AA0019List {
    userID: string;
    userName: string;
    userAlias: string;
    orgId: string;
    orgName: string;
    roleAlias: string;
    status: string;
    statusName: string;
}
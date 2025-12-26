import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0018 extends BaseReq {
    ReqBody: AA0018Req;
}
export interface AA0018Req {
    roleName: string; // user role
    roleNameMapping: Array<string>; // new role name
}

export interface ResAA0018 extends BaseRes {
    RespBody: AA0018Resp;
}
export interface AA0018Resp {
    roleName: string;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0016 extends BaseReq {
    ReqBody: AA0016Req;
}
export interface AA0016Req {
    roleName: string; // user role
    roleNameMapping: Array<string>; // new role name
}

export interface ResAA0016 extends BaseRes {
    RespBody: AA0016Resp;
}
export interface AA0016Resp {
    roleName: string;
}
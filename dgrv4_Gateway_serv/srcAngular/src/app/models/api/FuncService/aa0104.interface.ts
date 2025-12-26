import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0104 extends BaseReq {
    ReqBody: AA0104Req;
}
export interface AA0104Req {
    roleId?: string;
    keyword?: string;
    funcCode: string;
}

export interface ResAA0104 extends BaseRes {
    RespBody: AA0104Resp;
}
export interface AA0104Resp {
    roleInfoList: Array<AA0104List>;
}
export interface AA0104List {
    roleId: string;
    roleName: string;
    roleAlias: string;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1104 extends BaseReq {
    ReqBody: AA1104Req;
}
export interface AA1104Req {
    securityLevelId: string;
    securityLevelName: string;
}

export interface ResAA1104 extends BaseRes {
    RespBody: AA1104Resp;
}
export interface AA1104Resp { }
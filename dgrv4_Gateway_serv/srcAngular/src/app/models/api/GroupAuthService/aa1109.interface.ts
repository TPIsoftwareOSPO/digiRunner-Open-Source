import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1109 extends BaseReq {
    ReqBody: AA1109Req;
}
export interface AA1109Req {
    groupAuthoritieId: string;
}

export interface ResAA1109 extends BaseRes {
    RespBody: AA1109Resp;
}
export interface AA1109Resp {
    groupAuthoritieId: string;
    groupAuthoritieName: string;
    groupAuthoritieDesc: string;
    groupAuthoritieLevel: string;
}
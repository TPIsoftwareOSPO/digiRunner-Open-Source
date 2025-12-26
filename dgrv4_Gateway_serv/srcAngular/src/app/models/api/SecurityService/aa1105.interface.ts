import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1105 extends BaseReq {
    ReqBody: AA1105Req;
}
export interface AA1105Req {
    securityLevelId: string;
    securityLevelName: string;
}

export interface ResAA1105 extends BaseRes {
    RespBody: AA1105Resp;
}
export interface AA1105Resp {
    securityLevelId: string;
    securityLevelName: string;
    securityLevelDesc?: string;
}
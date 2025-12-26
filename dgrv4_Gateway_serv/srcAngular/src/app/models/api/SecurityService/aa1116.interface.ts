import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1116 extends BaseReq {
    ReqBody: AA1116Req;
}
export interface AA1116Req {
    securityLevelId?: string;
    keyword?: string;
}

export interface ResAA1116 extends BaseRes {
    RespBody: AA1116Resp;
}
export interface AA1116Resp {
    dataList: Array<AA1116Item>;
}
export interface AA1116Item {
    securityLevelId: string;
    securityLevelName: string;
    securityLevelDesc?: string;
}
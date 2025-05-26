import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0429 extends BaseReq {
    ReqBody: AA0429Req;
}
export interface AA0429Req { }

export interface RespAA0429 extends BaseRes {
    RespBody: AA0429Resp;
}

export interface AA0429Resp {
    targetSiteList: Array<string>;
}

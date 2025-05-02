import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0427 extends BaseReq {
    ReqBody: AA0427Req;
}
export interface AA0427Req { }

export interface RespAA0427 extends BaseRes {
    RespBody: AA0427Resp;
}

export interface AA0427Resp {
    labelList: Array<string>;
}

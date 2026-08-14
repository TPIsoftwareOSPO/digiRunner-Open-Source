import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0427 extends BaseReq {
    ReqBody: AA0427Req;
}
export interface AA0427Req {
    /** API 來源，空陣列或未帶則不做來源過濾 */
    apiSrc?: Array<string>;
}

export interface RespAA0427 extends BaseRes {
    RespBody: AA0427Resp;
}

export interface AA0427Resp {
    labelList: Array<string>;
}

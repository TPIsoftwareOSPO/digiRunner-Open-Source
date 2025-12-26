import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0319 extends BaseReq {
    ReqBody: AA0319Req;
}
export interface AA0319Req {
    batchNo: number;
    apiList: Array<AA0319ReqItem>;
}
export interface AA0319ReqItem {
    apiKey: string;
    moduleName: string;
}

export interface ResAA0319 extends BaseRes {
    RespBody: AA0319Resp;
}
export interface AA0319Resp {
    apiList: Array<AA0319RespItem>;
}
export interface AA0319RespItem {
    apiKey: string;
    moduleName: string;
    result: AA0319Pair;
    desc?: AA0318Trunc
}
export interface AA0319Pair {
    v: string;
    n?: string;
}
export interface AA0318Trunc {
    val: string;
    t: boolean;
    ori?: string;
}
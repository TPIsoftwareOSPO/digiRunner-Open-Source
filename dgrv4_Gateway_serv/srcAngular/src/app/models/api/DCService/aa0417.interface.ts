import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0417 extends BaseReq {
    ReqBody: AA0417Req;
}
export interface AA0417Req {
    node?: string;
    keyword?: string;
    excludeNode?: Array<string>;
}

export interface ResAA0417 extends BaseRes {
    RespBody: AA0417Resp;
}
export interface AA0417Resp {
    nodeList: Array<string>;
}
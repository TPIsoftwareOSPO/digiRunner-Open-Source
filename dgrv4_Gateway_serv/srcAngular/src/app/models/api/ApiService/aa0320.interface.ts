import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0320 extends BaseReq {
    ReqBody: AA0320Req;
}
export interface AA0320Req {
    keyword?: string;
    gId?: string;
    moduleName?: string;
    apiKey?: string;
}

export interface ResAA0320 extends BaseRes {
    RespBody: AA0320Resp;
}
export interface AA0320Resp {
    moduleName: string;
    apiKey: string;
    dataList: Array<AA0320Item>;
}
export interface AA0320Item {
    gId: string;
    name: string;
    alias?: string;
    desc?: string;
    v: string; // 'Y':是虛擬群組，'N':不是虛擬群組
    vId?: string; // v='Y'，才會有此欄位
}
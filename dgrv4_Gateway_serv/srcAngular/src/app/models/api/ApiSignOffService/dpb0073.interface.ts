import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0073 extends BaseReq {
    ReqBody: DPB0073Req;
}
export interface DPB0073Req {
    encodePublicFlag: string; // YYYY/MM/DD
    apiPKs: Array<DPB0073ApiList>; // YYYY/MM/DD
}
export interface DPB0073ApiList {
    apiKey?: string;
    moduleName?: string;
}

export interface ResDPB0073 extends BaseRes {
    RespBody: DPB0073Resp;
}
export interface DPB0073Resp { }
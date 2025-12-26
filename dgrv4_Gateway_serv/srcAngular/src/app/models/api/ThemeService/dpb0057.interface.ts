import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0057 extends BaseReq {
    ReqBody: DPB0057Req;
}
export interface DPB0057Req {
    delList: Array<number>;
}

export interface RespDPB0057 extends BaseRes {
    RespBody: DPB0057Resp;
}
export interface DPB0057Resp {
    resultMap: Map<number, string>;
    resultFileMap: Map<number, string>;
}
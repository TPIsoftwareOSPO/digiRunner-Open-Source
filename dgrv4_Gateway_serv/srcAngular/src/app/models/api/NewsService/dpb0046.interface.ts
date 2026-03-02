import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0046 extends BaseReq {
    ReqBody: DPB0046Req;
}
export interface DPB0046Req {
    delList: Array<number>;
}

export interface RespDPB0046 extends BaseRes {
    RespBody: DPB0046Resp;
}
export interface DPB0046Resp {
    resultMap: Map<number, string>;
}
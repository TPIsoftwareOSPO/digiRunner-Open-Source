import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0016 extends BaseReq {
    ReqBody: DPB0016Req;
}
export interface DPB0016Req {
    appId: number;
}

export interface ResDPB0016 extends BaseRes {
    RespBody: DPB0016Resp;
}
export interface DPB0016Resp { }
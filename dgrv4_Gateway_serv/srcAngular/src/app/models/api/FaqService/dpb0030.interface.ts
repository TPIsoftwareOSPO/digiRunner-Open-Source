import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0030 extends BaseReq {
    ReqBody: DPB0030Req;
}
export interface DPB0030Req {
    questionId: number;
}

export interface ResDPB0030 extends BaseRes {
    RespBody: DPB0030Resp;
}
export interface DPB0030Resp { }
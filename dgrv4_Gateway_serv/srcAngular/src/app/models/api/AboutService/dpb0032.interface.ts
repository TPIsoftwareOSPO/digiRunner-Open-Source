import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0032 extends BaseReq {
    ReqBody: DPB0032Req;
}
export interface DPB0032Req { }

export interface ResDPB0032 extends BaseRes {
    RespBody: DPB0032Resp;
}
export interface DPB0032Resp {
    seqId: number;
    aboutSubject: string;
    aboutDesc: string;
}
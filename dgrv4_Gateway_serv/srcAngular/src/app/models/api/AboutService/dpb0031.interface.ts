import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0031 extends BaseReq {
    ReqBody: DPB0031Req;
}
export interface DPB0031Req {
    seqId: number;
    aboutSubject: string;
    aboutDesc: string;
}

export interface ResDPB0031 extends BaseRes {
    RespBody: DPB0031Resp;
}
export interface DPB0031Resp { }
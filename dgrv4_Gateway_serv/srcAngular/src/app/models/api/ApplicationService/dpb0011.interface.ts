import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0011 extends BaseReq {
    ReqBody: DPB0011Req;
}
export interface DPB0011Req {
    appCateId: number;
}

export interface ResDPB0011 extends BaseRes {
    RespBody: DPB0011Resp;
}
export interface DPB0011Resp { }
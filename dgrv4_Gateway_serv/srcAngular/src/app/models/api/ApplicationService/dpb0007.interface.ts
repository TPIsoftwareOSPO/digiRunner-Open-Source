import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0007 extends BaseReq {
    ReqBody: DPB0007Req;
}
export interface DPB0007Req {
    appCateName: string;
}

export interface ResDPB0007 extends BaseRes {
    RespBody: DPB0007Resp;
}
export interface DPB0007Resp { }
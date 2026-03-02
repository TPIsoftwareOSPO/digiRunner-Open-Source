import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0108 extends BaseReq {
    ReqBody: DPB0108Req;
}
export interface DPB0108Req {
    eventId: number;
    keepFlag: string;
}

export interface ResDPB0108 extends BaseRes {
    RespBody: DPB0108Resp;
}
export interface DPB0108Resp { }
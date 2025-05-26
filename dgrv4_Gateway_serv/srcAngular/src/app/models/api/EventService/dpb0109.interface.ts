import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0109 extends BaseReq {
    ReqBody: DPB0109Req;
}
export interface DPB0109Req {
    eventId: number;
    archiveFlag: string;
}

export interface ResDPB0109 extends BaseRes {
    RespBody: DPB0109Resp;
}
export interface DPB0109Resp { }
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0062 extends BaseReq {
    ReqBody: DPB0062Req;
}
export interface DPB0062Req {
    refItemNo: string;
    refSubitemNo: string;
    startDateTime: string;
    inParams: string; // base64
    identifData: string; // base64
}

export interface ResDPB0062 extends BaseRes {
    RespBody: DPB0062Resp;
}
export interface DPB0062Resp {
    apptJobId: number;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0060 extends BaseReq {
    ReqBody: DPB0060Req;
}
export interface DPB0060Req {
    apptJobId: number;
    lv: number;
}

export interface ResDPB0060 extends BaseRes {
    RespBody: DPB0060Resp;
}
export interface DPB0060Resp {
    apptJobId: number;
    status: string;
    lv: number;
}
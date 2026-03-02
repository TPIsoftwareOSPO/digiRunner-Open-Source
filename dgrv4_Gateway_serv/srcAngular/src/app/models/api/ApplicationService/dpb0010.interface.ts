import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0010 extends BaseReq {
    ReqBody: DPB0010Req;
}
export interface DPB0010Req {
    appCateId: number;
    appCateName: string;
}

export interface ResDPB0010 extends BaseRes {
    RespBody: DPB0010Resp;
}
export interface DPB0010Resp { }
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0098 extends BaseReq {
    ReqBody: DPB0098Req;
}
export interface DPB0098Req {
    tsmpRtnCode: string;
    locale: string;
}

export interface ResDPB0098 extends BaseRes {
    RespBody: DPB0098Resp;
}
export interface DPB0098Resp {
    tsmpRtnCode: string;
    tsmpRtnMsg: string;
    tsmpRtnDesc?: string;
    locale: string;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0100 extends BaseReq {
    ReqBody: DPB0100Req;
}
export interface DPB0100Req {
    tsmpRtnCode: string;
    locale: string;
}

export interface ResDPB0100 extends BaseRes {
    RespBody: DPB0100Resp;
}
export interface DPB0100Resp {
    tsmpRtnCode?: string;
    tsmpRtnMsg?: string;
    tsmpRtnDesc?: string;
    locale?: string;
}
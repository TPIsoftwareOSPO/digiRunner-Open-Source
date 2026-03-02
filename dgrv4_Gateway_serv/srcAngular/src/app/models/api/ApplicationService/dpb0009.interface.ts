import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0009 extends BaseReq {
    ReqBody: DPB0009Req;
}
export interface DPB0009Req {
    appCateId: number;
}

export interface ResDPB0009 extends BaseRes {
    RespBody: DPB0009Resp;
}
export interface DPB0009Resp {
    appCateId: number;
    appCateName: string;
    dataSort: number;
    createDateTime: string;
    updateDateTime: string;
}
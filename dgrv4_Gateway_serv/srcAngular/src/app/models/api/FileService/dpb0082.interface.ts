import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0082 extends BaseReq {
    ReqBody: DPB0082Req;
}
export interface DPB0082Req { }

export interface ResDPB0082 extends BaseRes {
    RespBody: DPB0082Resp;
}
export interface DPB0082Resp {
    tempFileName: string;
}
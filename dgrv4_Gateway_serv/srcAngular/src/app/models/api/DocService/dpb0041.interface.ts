import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0041 extends BaseReq {
    ReqBody: DPB0041Req;
}
export interface DPB0041Req {
    moduleNames: string;
}

export interface ResDPB0041 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { }
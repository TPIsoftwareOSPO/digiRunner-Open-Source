import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9904 extends BaseReq {
    ReqBody: DPB9904Req;
}

export interface DPB9904Req {
    id: string;    
}

export interface RespDPB9904 extends BaseRes {
    RespBody: DPB9904Resp;
}

export interface DPB9904Resp { }

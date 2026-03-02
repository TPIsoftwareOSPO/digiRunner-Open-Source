import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0125 extends BaseReq {
    ReqBody: DPB0125Req;
}

export interface DPB0125Req {
    timeS: string,
    timeE: string,
    idxName: string,
}

export interface RespDPB0125 extends BaseRes {
    RespBody: DPB0125Resp;
}

export interface DPB0125Resp { 
    listObject?:Array<Object>;
}
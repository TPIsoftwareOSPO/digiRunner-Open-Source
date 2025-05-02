import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0126 extends BaseReq {
    ReqBody: DPB0126Req;
}

export interface DPB0126Req {
    keyword?: string;
    roleIndexList?: Array<string>
}

export interface RespDPB0126 extends BaseRes {
    RespBody: DPB0126Resp;
}

export interface DPB0126Resp { 
    indexList?:Array<string>;
}
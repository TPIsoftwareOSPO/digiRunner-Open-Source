import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0035 extends BaseReq {
    ReqBody: DPB0035Req;
}
export interface DPB0035Req {
    siteId: number;
}

export interface ResDPB0035 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { }
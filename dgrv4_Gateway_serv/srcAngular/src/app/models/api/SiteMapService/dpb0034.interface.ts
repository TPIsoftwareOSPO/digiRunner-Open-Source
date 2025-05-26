import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0034 extends BaseReq {
    ReqBody: DPB0034Req;
}
export interface DPB0034Req {
    siteId: number;
    siteDesc: string;
    siteUrl?: string;
}

export interface ResDPB0034 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { }
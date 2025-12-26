import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0033 extends BaseReq {
    ReqBody: DPB0033Req;
}
export interface DPB0033Req {
    siteParentId: number;
    siteDesc: string;
    siteUrl?: string;
}

export interface ResDPB0033 extends BaseRes {
    RespBody: DPB0033Node;
}
export interface DPB0033Node {
    siteId: number;
    siteParentId: number;
    siteDesc: string;
    dataSort: number;
    siteUrl: string;
}
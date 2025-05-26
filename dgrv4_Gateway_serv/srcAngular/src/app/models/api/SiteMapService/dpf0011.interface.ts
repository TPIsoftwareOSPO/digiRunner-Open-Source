import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPF0011 extends BaseReq {
    ReqBody: DPF0011Req;
}
export interface DPF0011Req { }

export interface ResDPF0011 extends BaseRes {
    RespBody: DPF0011Node;
}
export interface DPF0011Node {
    siteId: number;
    siteParentId: number;
    siteDesc: string;
    siteUrl: string;
    nodes: Array<DPF0011Node>;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0095 extends BaseReq {
    ReqBody: DPB0095Req;
}
export interface DPB0095Req {
    clientId?: string;
    keyword?: string;
    regStatus: string;
}

export interface RespDPB0095 extends BaseRes {
    RespBody: DPB0095Resp;
}
export interface DPB0095Resp {
    dataList: Array<DPB0095Item>;
}
export interface DPB0095Item {
    clientId: string;
    clientName: string;
    clientAlias?: string;
    emails?: string;
    clientStatus: string;
} 
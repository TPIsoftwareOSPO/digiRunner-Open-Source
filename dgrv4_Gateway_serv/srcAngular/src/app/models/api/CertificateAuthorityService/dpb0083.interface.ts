import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0083 extends BaseReq {
    ReqBody: DPB0083Req;
}
export interface DPB0083Req {
    clientId?: string; //分頁用
    keyword?: string;
}

export interface ResDPB0083 extends BaseRes {
    RespBody: DPB0083Resp;
}
export interface DPB0083Resp {
    dataList: Array<DPB0083RespItem>;
}
export interface DPB0083RespItem {
    clientId: string;
    clientName: string;
    clientAlias?: string;
    emails?: string;
    clientStatus: string;
} 
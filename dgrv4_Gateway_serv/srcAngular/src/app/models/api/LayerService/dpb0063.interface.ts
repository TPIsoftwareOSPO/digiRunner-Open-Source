import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0063 extends BaseReq {
    ReqBody: DPB0063Req;
}
export interface DPB0063Req {
    dataMap: object; // { reviewType: { layer: Array<DPB0063PkReq> } }
}
export interface DPB0063PkReq {
    reviewType: string;
    layer: number;
    roleId: string;
    lv?: number;
}

export interface ResDPB0063 extends BaseRes {
    RespBody: DPB0063Resp;
}
export interface DPB0063Resp {
    dataMap: object; // { reviewType: { layer: Array<DPB0063SaveItem> } }
}
export interface DPB0063SaveItem {
    chkLayerId: number;
    reviewType: string;
    layer: string;
    roleId: string;
    status: string;
    lv: number;
}
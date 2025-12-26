import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0047 extends BaseReq {
    ReqBody: DPB0047Req;
}
export interface DPB0047Req {
    itemId?: number;
    keyword?: string;
    encodeItemNo: string;
    isDefault: string;
}

export interface ResDPB0047 extends BaseRes {
    RespBody: DPB0047Resp;
}
export interface DPB0047Resp {
    subItems?: Array<DPB0047SubItems>;
    defaultVal?: string;
}
export interface DPB0047SubItems {
    itemId: number;
    itemNo: string;
    itemName: string;
    subitemNo: string;
    subitemName: string;
    sortBy: number;
    isDefault?: string;
    param1?: string;
    param2?: string;
    param3?: string;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9905 extends BaseReq {
    ReqBody: DPB9905Req;
}

export interface DPB9905Req {
    p_itemOrder?: number;
    p_sortBy?: number;
    locale: string;
    keyword?: string;
    itemNo?: string;
}

export interface RespDPB9905 extends BaseRes {
    RespBody: DPB9905Resp;
}

export interface DPB9905Resp {
    itemList:Array<DPB9905Item>;
}

export interface DPB9905Item {
    itemOrder: number|null;
    itemNo: string;
    itemName: string;
    updateDateTime: string;
    updateUser: string;
    subitemCount: string;
    subitemList?:Array<DPB9905Subitem>
}

export interface DPB9905Subitem {
    subitemOrder: string;
    sortBy: number|null;
    subitemNo: string;
    subitemName: string;
    updateDateTime: string;
    updateUser: string;
    isDefault?: string|null;
}


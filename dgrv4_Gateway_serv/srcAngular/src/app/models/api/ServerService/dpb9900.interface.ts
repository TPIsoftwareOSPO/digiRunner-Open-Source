import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9900 extends BaseReq {
    ReqBody: DPB9900Req;
}

export interface DPB9900Req {
    id?: string;
    key?: string;    
}

export interface RespDPB9900 extends BaseRes {
    RespBody: DPB9900Resp;
}

export interface DPB9900Resp {
    dataList: Array<DPB9900Item>;    
}

export interface DPB9900Item {
    id: string;
    value: DPB9900Trunc;
    memo?: DPB9900Trunc;
}

export interface DPB9900Trunc {
    val: string; // 顯示用
    t: boolean; // 是否被截斷
    ori?: string; // 原始值，被截斷才回傳
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9910 extends BaseReq {
    ReqBody: DPB9910Req;
}

export interface DPB9910Req {
    sortBy?: number;
    keyword?: string;    
}

export interface RespDPB9910 extends BaseRes {
    RespBody: DPB9910Resp;
}

export interface DPB9910Resp {
    itemList: Array<DPB9910Item>;    
}

export interface DPB9910Item {
    cusSettingId: string;
    settingNo: string;
    settingName: DPB9910Trunc;
    subsettingNo: string;
    subsettingName: DPB9910Trunc;
    sortBy:number;
}

export interface DPB9910Trunc {
    val: string; // 顯示用
    t: boolean; // 是否被截斷
    ori?: string; // 原始值，被截斷才回傳
}
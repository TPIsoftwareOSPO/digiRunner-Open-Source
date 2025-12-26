import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0071 extends BaseReq {
    ReqBody: DPB0071Req;
}
export interface DPB0071Req {
    reqOrdermId: number;
    encodeSubItemNo: string; // bcrypt subItem
    chkStatus: string;
    reqComment?: string;
}

export interface ResDPB0071 extends BaseRes {
    RespBody: DPB0071Resp;
}
export interface DPB0071Resp {
    chkLogId: number;
    reqOrdersId: number;
    lv: number;
}
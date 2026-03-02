import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0118 extends BaseReq { }

export interface ResDPB0118 extends BaseRes {
    RespBody: DPB0118Resp;
}
export interface DPB0118Resp {
    majorVersionNo: string; // digiRunner 主版本號，tsmpdpaa 中版號+1
    edition:string;
    expiryDate:string;
    version:string;
    nearWarnDays:number;
    overBufferDays:number;
    isExpired:boolean;
}


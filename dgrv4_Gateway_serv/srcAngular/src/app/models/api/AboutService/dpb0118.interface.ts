import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0118 extends BaseReq { }

export interface ResDPB0118 extends BaseRes {
    RespBody: DPB0118Resp;
}
export interface DPB0118Resp {
    majorVersionNo: string; // digiRunner 主版本號，tsmpdpaa 中版號+1
    // dataList: Array<DPB0118Item>;
    edition:string;
    expiryDate:string;
    version:string;
    nearWarnDays:number;
    overBufferDays:number;


}
// export interface DPB0118Item {
//     moduleName: string;
//     moduleVersion: string;
// }

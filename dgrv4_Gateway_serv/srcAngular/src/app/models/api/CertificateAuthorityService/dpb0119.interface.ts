import { BaseReq, BaseRes } from '../base.interface';
export interface ResDPB0119 extends BaseRes {
    RespBody: DPB0119Resp;
}
export interface DPB0119Resp {
    cusUrl: string;
}
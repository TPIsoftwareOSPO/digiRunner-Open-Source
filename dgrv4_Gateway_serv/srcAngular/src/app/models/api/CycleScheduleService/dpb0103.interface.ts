import { BaseReq, BaseRes } from '../base.interface';
import { DPB0101Cron } from './dpb0101.interface';

export interface ReqDPB0103 extends BaseReq {
    ReqBody: DPB0103Req;
}
export interface DPB0103Req {
    apptRjobId: string;
}

export interface RespDPB0103 extends BaseRes {
    RespBody: DPB0103Resp;
}
export interface DPB0103Resp {
    apptRjobId: string;
    lv: number;
    rjobName: string;
    remark?: string;
    cronJson: DPB0103Cron;
    cronExpression: string;
    effDateTime?: string;
    invDateTime?: string;
    status: string;
    oriDataList: Array<DPB0103Items>;
}
export interface DPB0103Cron extends DPB0101Cron { }
export interface DPB0103Items {
    apptRjobDId: number;
    lv: number;
    refItemNo: string;
    refItemName: string;
    refSubitemNo?: string;
    refSubitemName?: string;
    inParams?: string;
    identifData?: string;
    sortBy: number;
} 
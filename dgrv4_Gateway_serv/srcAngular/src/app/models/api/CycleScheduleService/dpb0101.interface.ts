import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB0101 extends BaseReq {
    ReqBody: DPB0101Req;
}
export interface DPB0101Req {
    rjobName: string;
    remark?: string;
    cronJson: DPB0101Cron;
    effDateTime?: string;
    invDateTime?: string;
    rjobItems: Array<DPB0101Items>;
}
export interface DPB0101Cron {
    frequency: number;
    dayRange: Array<number>;
    weekRange: Array<number>;
    hour: number;
    minute: number;
}
export interface DPB0101Items {
    refItemNo: string;
    refSubitemNo?: string;
    inParams?: string;
    identifData?: string;
    sortBy: number;
    isValid?:boolean;
}
export interface DPB0101ItemsAddNo extends DPB0101Items {
    no: number;
}

export interface RespDPB0101Before extends BaseRes {
    RespBody: DPB0101RespBefore;
}
export interface DPB0101RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface RespDPB0101 extends BaseRes {
    RespBody: DPB0101Resp;
}
export interface DPB0101Resp {
    apptRjobId: string;
    lv: string;
    cronExpression: string;
    nextDateTime: string;
    status: string;
    statusName: string;
    apptRjobDIds: Array<object>; // [{ apptRjobDId: lv }];
}
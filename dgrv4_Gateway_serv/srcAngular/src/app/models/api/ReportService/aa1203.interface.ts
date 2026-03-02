import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1203 extends BaseReq {
    ReqBody: AA1203Req;
}
export interface AA1203Req {
    timeType: string;
    apiUidList?: Array<string>;
    startDate: string;
    endDate: string;
    startHour?: string;
    endHour?: string
}

export interface ResAA1203Before extends BaseRes {
    RespBody: AA1203RespBefore;
}
export interface AA1203RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1203 extends BaseRes {
    RespBody: AA1203Resp;
}
export interface AA1203Resp {
    labels: Array<string>;
    datasets: Array<AA1203DataSetResp>;
    xLable: string;
    reportName: string;
}
export interface AA1203DataSetResp {
    label: string;
    data: Array<number>;
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1201 extends BaseReq {
    ReqBody: AA1201Req;
}
export interface AA1201Req {
    timeType: string;
    apiUidList?: Array<string>;
    startDate: string;
    endDate: string;
    startHour?: string;
    endHour?: string
}

export interface ResAA1201Before extends BaseRes {
    RespBody: AA1201RespBefore;
}
export interface AA1201RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1201 extends BaseRes {
    RespBody: AA1201Resp;
}
export interface AA1201Resp {
    labels: Array<string>;
    datasets: Array<AA1201DataSetResp>;
    xLable: string;
    reportName: string;
}
export interface AA1201DataSetResp {
    label: string;
    data: Array<number>;
}

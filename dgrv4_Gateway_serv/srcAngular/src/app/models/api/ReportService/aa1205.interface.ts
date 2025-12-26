import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1205 extends BaseReq {
    ReqBody: AA1205Req;
}
export interface AA1205Req {
    timeType: string;
    startDate: string;
    endDate: string;
    startHour?: string;
    endHour?: string
}

export interface ResAA1205Before extends BaseRes {
    RespBody: AA1205RespBefore;
}
export interface AA1205RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1205 extends BaseRes {
    RespBody: AA1205Resp;
}
export interface AA1205Resp {
    labels: Array<string>;
    datasets: Array<AA1205DataSetResp>;
    xLable: string;
    yLable: string;
    reportName: string;
}
export interface AA1205DataSetResp {
    label: string;
    data: Array<number>;
}

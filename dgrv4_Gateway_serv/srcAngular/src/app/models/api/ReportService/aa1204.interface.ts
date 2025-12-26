import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1204 extends BaseReq {
    ReqBody: AA1204Req;
}
export interface AA1204Req {
    timeType: string;
    startDate: string;
    endDate: string;
    startHour?: string;
    endHour?: string
}

export interface ResAA1204Before extends BaseRes {
    RespBody: AA1204RespBefore;
}
export interface AA1204RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1204 extends BaseRes {
    RespBody: AA1204Resp;
}
export interface AA1204Resp {
    labels: Array<string>;
    datasets: Array<AA1204DataSetResp>;
    xLable: string;
    yLable: string;
    reportName: string;
}
export interface AA1204DataSetResp {
    label: string;
    data: Array<number>;
}

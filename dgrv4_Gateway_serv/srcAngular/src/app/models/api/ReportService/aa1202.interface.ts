import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1202 extends BaseReq {
    ReqBody: AA1202Req;
}
export interface AA1202Req {
    timeType: string;
    apiUidList?: Array<string>;
    startDate: string;
    endDate: string;
    startHour?: string;
    endHour?: string
}

export interface ResAA1202Before extends BaseRes {
    RespBody: AA1202RespBefore;
}
export interface AA1202RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA1202 extends BaseRes {
    RespBody: AA1202Resp;
}
export interface AA1202Resp {
    labels: Array<string>;
    datasets: Array<AA1202DataSetResp>;
    xLable: string;
    yLable: string;
    reportName: string;
}
export interface AA1202DataSetResp {
    label: string;
    data: Array<number>;
}

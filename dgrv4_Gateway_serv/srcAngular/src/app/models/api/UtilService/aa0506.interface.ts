import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0506: getReportUrls
 * 預設取得所有報表Today的URLs。
 */
export interface ReqAA0506 extends BaseReq {
    ReqBody: AA0506Req;
}
export interface AA0506Req {
    reportID: string;
    timeRange: string;
}

export interface ResAA0506 extends BaseRes {
    RespBody: AA0506Resp;
}
export interface AA0506Resp {
    rpport: number;
    reportID: string;
    reportUrl: string;
    rpContentPath: string;
    reportType: string;
}

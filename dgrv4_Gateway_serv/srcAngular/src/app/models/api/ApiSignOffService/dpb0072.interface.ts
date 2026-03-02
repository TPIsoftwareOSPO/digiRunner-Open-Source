import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0072 extends BaseReq {
    ReqBody: DPB0072Req;
}
export interface DPB0072Req {
    apiKey?: string;
    moduleName?: string;
    keyword?: string;
    startDate: string; // YYYY/MM/DD
    endDate: string; // YYYY/MM/DD
    orgFlagEncode: string;
}

export interface ResDPB0072 extends BaseRes {
    RespBody: DPB0072Resp;
}
export interface DPB0072Resp {
    dataList: Array<DPB0072RespItem>;
}
export interface DPB0072RespItem {
    apiKey: string;
    moduleName: string;
    apiUid: string;
    apiName: string;
    apiDesc: string;
    themeName: string;
    orgName: string;
    dpStuDateTime: string;
    apiExtId: number;
    fileName?: string;
    filePath?: string;
    publicFlag: string;
    publicFlagName: string;
}
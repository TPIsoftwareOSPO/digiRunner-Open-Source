import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0075 extends BaseReq {
    ReqBody: DPB0075Req;
}
export interface DPB0075Req {
    apiKey?: string;
    moduleName?: string;
    keyword?: string;
    dpStatus: string; // 0：下架，1：上架
}

export interface ResDPB0075 extends BaseRes {
    RespBody: DPB0075Resp;
}
export interface DPB0075Resp {
    dataList: Array<DPB0075RespItem>;
}
export interface DPB0075RespItem {
    apiKey?: string;
    moduleName?: string;
    apiName: string;
    themeDatas: object; // Map<themeId: number, themeName: string>() => {1: 'theme 1',2:'theme'}
    orgId?: string;
    orgName?: string;
    apiDesc?: string;
    dpStatus?: string;
    apiExtId?: number;
    apiUid: string;
    fileName?: string;
    filePath?: string;
    publicFlag?: string;
    publicFlagName?: string;
}
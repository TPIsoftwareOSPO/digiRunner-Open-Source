import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0321 extends BaseReq {
    ReqBody: AA0321Req;
}
export interface AA0321Req {
    apiKey?: string;
    moduleName?: string;
    keyword?: string;
    apiUidList: Array<string>;
}

export interface ResAA0321 extends BaseRes {
    RespBody: AA0321Resp;
}
export interface AA0321Resp {
    dataList: Array<AA0321RespItem>;
}
export interface AA0321RespItem {
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
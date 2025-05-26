import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0093 extends BaseReq {
    ReqBody: DPB0093Req;
}
export interface DPB0093Req {
    apiKey?: string;
    moduleName?: string; // itemNo = 'OPEN_API_KEY'
    keyword?: string;
}

export interface ResDPB0093 extends BaseRes {
    RespBody: DPB0093Resp;
}
export interface DPB0093Resp {
    dataList: Array<DPB0093ApiItem>;
}
export interface DPB0093ApiItem {
    apiKey: string;
    moduleName: string;
    apiName: string;
    themeDatas: object; // { themeId: themeName }
    orgId: string;
    orgName: string;
    apiDesc: string;
    apiExtId: number;
    apiUid: string;
    fileName?: string;
    filePath?: string;
} 
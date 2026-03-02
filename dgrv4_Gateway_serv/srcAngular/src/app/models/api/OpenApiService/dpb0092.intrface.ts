import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0092 extends BaseReq {
    ReqBody: DPB0092Req;
}
export interface DPB0092Req {
    clientId: string;
    encodeReqSubtype: string; // itemNo = 'OPEN_API_KEY'
    openApiKeyId?: number;
}

export interface ResDPB0092 extends BaseRes {
    RespBody: DPB0092Resp;
}
export interface DPB0092Resp {
    clientId: string;
    clientName: string;
    clientAlias: string;
    reqDate: string;
    userName: string;
    orgId: string;
    orgName: string;
    effectiveDate: string;
    reqSubtype: string;
    reqSubtypeName: string;
    openApiKey?: string;
    secretKey?: string;
    openApiKeyAlias?: string;
    expiredAt?: string;
    timesThreshold?: string;
    dataList?: Array<DPB0092ApiItem>;
}
export interface DPB0092ApiItem {
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
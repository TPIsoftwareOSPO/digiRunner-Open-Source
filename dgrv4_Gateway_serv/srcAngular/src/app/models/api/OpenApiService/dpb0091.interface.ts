import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0091 extends BaseReq {
    ReqBody: DPB0091Req;
}
export interface DPB0091Req {
    apiKey?: string;
    moduleName?: string;
    openApiKeyId: number;
}

export interface ResDPB0091 extends BaseRes {
    RespBody: DPB0091Resp;
}
export interface DPB0091Resp {
    openApiKeyId: number;
    clientId: string;
    clientName: string;
    clientAlias: string;
    openApiKey: string;
    secretKey: string;
    openApiKeyAlias: string;
    timesQuota?: number;
    timesThreshold?: number;
    createDateTime: string;
    expiredAt: string;
    revokedAt: string;
    openApiKeyStatus: string;
    openApiKeyStatusName: string;
    dataList: Array<DPB0091ApiItem>;
}
export interface DPB0091ApiItem {
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
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0211: 新增Group
 * 在TSMP中新增TSMP API Client Group，簡稱Group。系統可依Client的Group決定API使用權限。
 * 新增成功時返回groupID。
 * 權限設定時，只管Group是否有權使用該API，不管API的Module版本。換句話說，權限不會細到API要屬於哪個Module版本。
 */
export interface ReqAA0211 extends BaseReq {
    ReqBody: AA0211Req;
}
export interface AA0211Req {
    groupName: string;
    groupAlias?: string;
    groupDesc?: string;
    securityLevel?: string;
    allowAccessUseTimesTimeUnit?: string;
    groupAuthorities?: Array<string>;
    apiKeyList?: Array<object>;
    allowAccessDays?: number;
    allowAccessUseTimes?: number;
}
export interface APIKey {
    apiKey: string;
    moduleName: string;
}

export interface ResAA0211 extends BaseRes {
    RespBody: AA0211Resp;
}
export interface AA0211Resp {
    groupID: string;
}


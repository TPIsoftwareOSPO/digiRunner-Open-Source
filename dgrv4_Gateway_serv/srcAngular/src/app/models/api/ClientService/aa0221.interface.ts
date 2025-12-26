import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0221: 新增Virtul Group
 */
export interface ReqAA0221 extends BaseReq {
    ReqBody: AA0221Req;
}
export interface AA0221Req {
    vgroupName: string;
    vgroupAlias?: string;
    allowDays?: number;
    timeUnit: string;
    allowTimes?: number;
    vgroupAuthoritieIds?: Array<string>;
    securityLevelId?: string;
    vgroupDesc?: string;
    dataList?: Array<AA0224Item>;

    //之後要刪掉
    securityLevel?: string;
    vgroupAccess?: Array<string>;
    vgroupAuthorities?: Array<string>;
    apiKeyList?: Array<APIKey>;
    allowAccessDays?: number;
    allowAccessUseTimes?: number;
}

export interface AA0224Item {
    moduleName: string;
    apiKeyList: Array<string>;
}

//之後要刪掉
export interface APIKey {
    apiKey: string;
    moduleName: string;
}
export interface ResAA0221 extends BaseRes {
    RespBody: AA0221Resp;
}
export interface AA0221Resp {
    vgroupID: string;
}


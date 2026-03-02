import { APIKey } from './aa0211.interface';
import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0213: 查詢GroupDetail
 * 在TSMP中查詢Group詳細資料。
 */
export interface ReqAA0213 extends BaseReq {
    ReqBody: Req_0213;
}
export interface Req_0213 {
    groupID: string;
}

export interface ResAA0213 extends BaseRes {
    RespBody: Res_0213;
}
export interface Res_0213 {
    timeUnit: any;
    groupID: string;
    groupName: string;
    groupAlias: string;
    groupDesc: string;
    securityLevel: AA0213SecurityLevel;
    groupAccess: Array<string>;
    groupAuthorities: Array<AA0213GroupAuthorities>;
    allowAccessDays: number;
    allowAccessUseTimes: number;
    createDate: string;
    createUser: string;
    updateDate?: string;
    updateUser?: string;
    apiKeyList?: Array<APIKey>;
    approximateTimeUnit?: String;
}
export interface AA0213GroupAuthorities {
    groupAuthoritiedId: string;
    groupAuthoritiedName: string;
}
export interface AA0213SecurityLevel {
    securityLevelId: string;
    securityLevelName: string;
}
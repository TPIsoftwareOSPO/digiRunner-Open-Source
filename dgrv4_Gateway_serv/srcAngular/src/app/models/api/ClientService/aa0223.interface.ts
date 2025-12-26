import { APIKey } from './aa0211.interface';
import { BaseReq, BaseRes } from '../base.interface';
import { TTSecurityLV } from '../GroupAuthService/aa1102.interface';

/**
 * AA0223: 查詢GroupDetail
 * 在TSMP中查詢Group詳細資料。
 */
export interface ReqAA0223 extends BaseReq {
    ReqBody: AA0223Req;
}
export interface AA0223Req {
    vgroupId: string;
    vgroupName: string;

    //之後要刪掉
    vgroupID: string;
}

export interface ResAA0223 extends BaseRes {
    RespBody: AA0223Resp;
}
export interface AA0223Resp {
    vgroupId: string;
    vgroupName: string;
    vgroupAlias: string;
    vgroupDesc: string;
    securityLevel: TTSecurityLV;
    vgroupAccess: Array<string>;
    vgroupAuthorities: Array<AA0223Auth>;
    allowAccessDays: number;
    allowAccessUseTimes: number;
    createDate: string;
    createUser: string;
    updateDate?: string;
    updateUser?: string;
    apiKeyList?: Array<APIKey>;
    vgroupID: string;
}
export interface AA0223Auth {
    groupAuthoritieId: string;
    groupAuthoritieName: string;
}
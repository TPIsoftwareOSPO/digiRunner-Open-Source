import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0238: 查詢GroupList
 * 在TSMP中查詢所有Group清單。
 */
export interface ReqAA0238 extends BaseReq {
    ReqBody: AA0238Req;
}
export interface AA0238Req {
    groupId?: string;
    keyword?: string;
    securityLevelID?: string;
    groupAuthoritiesID?: Array<string>;
}

export interface ResAA0238 extends BaseRes {
    RespBody: AA0238Resp;
}
export interface AA0238Resp {
    listCount: number;
    groupInfoList: Array<GroupInfo_0238>;
}
export interface GroupInfo_0238 {
    groupID: string;
    groupName: string;
    groupAlias: string;
    groupAuthorities: Array<AA0238GroupAuthoritiesInfo>;
    securityLevelID: string;
    securityLevelName: string;
    groupDesc: string;
    createTime: string;
}
export interface AA0238GroupAuthoritiesInfo {
    groupAuthoritieName: string;
    oriGroupAuthoritieName: string;
    isTruncated: boolean;
}
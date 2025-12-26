import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0228 extends BaseReq {
    ReqBody: AA0228Req;
}
export interface AA0228Req {
    groupId?: string;
    keyword?: string;
    securityLevelID: string;
    clientID: string;
}

export interface ResAA0228 extends BaseRes {
    RespBody: AA0228Resp;
}
export interface AA0228Resp {
    groupInfoList: Array<AA0228GroupInfo>;
}
export interface AA0228GroupInfo {
    securityLevelName: string;
    groupName: string;
    groupID: string;
    groupDesc: string;
    groupAlias: String;
    createTime: String;
    moduleAPIKeyList: Array<AA0228ModuleAPIKey>;
}
export interface AA0228ModuleAPIKey {
    moduleName: string;
    apiNameApiKey: string;
    orgApiNameApiKey: string;
    isTruncated: boolean;
}
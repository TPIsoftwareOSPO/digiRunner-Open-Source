import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0229 extends BaseReq {
    ReqBody: AA0229Req;
}
export interface AA0229Req {
    vgroupId?: string;
    keyword?: string;
    securityLevelID: string;
    clientID: string;
}

export interface ResAA0229 extends BaseRes {
    RespBody: AA0229Resp;
}
export interface AA0229Resp {
    vgroupInfoList: Array<AA0229VgroupInfo>;
}
export interface AA0229VgroupInfo {
    securityLevelName: string;
    vgroupName: string;
    vgroupID: string;
    vgroupDesc: string;
    vgroupAlias: String;
    moduleAPIKeyList: Array<AA0229ModuleAPIKey>;
}
export interface AA0229ModuleAPIKey {
    moduleName: string;
    apiNameApiKey: string;
    orgApiNameApiKey: string;
    isTruncated: boolean;
}
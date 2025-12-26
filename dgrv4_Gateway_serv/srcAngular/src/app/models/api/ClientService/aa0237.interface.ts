import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0235: 查詢群組所擁有的Module
 */
export interface ReqAA0237 extends BaseReq {
    ReqBody: AA0237Req;
}
export interface AA0237Req {
    vgroupId: string;
    reqB: GroupInfo_0237;
}

export interface ResAA0237 extends BaseRes {
    RespBody: AA0237Resp;
}
export interface AA0237Resp {
    respA: AA0237RespA;
    respB1: AA0237RespB1;
    respB2: AA0237RespB2;
}

export interface GroupInfo_0237 {
    moduleName?: String;
    p?: Boolean;
    groupId?: String;
    apiKey?: String;
    keyword?: String;
}

export interface AA0237RespA {
    moduleNameList: Array<String>;
}

export interface AA0237RespB1 {
    apiKeyList: Array<AA0237RespB1D>;
}

export interface AA0237RespB1D {
    groupId: String;
    moduleName: String;
    apiKey: String;
    apiName: String;
}

export interface AA0237RespB2 {
    dataList: Array<AA0237RespB2D1>;
}

export interface AA0237RespB2D1 {
    moduleName: String;
    apiKeyList: Array<String>;
    isApiKeyTrunc: Boolean;
}

export interface AA0237RespB2D1 {
    apiKey: String;
    apiName: String;
}
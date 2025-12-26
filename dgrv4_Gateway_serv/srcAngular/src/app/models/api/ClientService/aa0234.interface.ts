import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0235: 查詢群組所擁有的Module
 */
export interface ReqAA0234 extends BaseReq {
    ReqBody: AA0234Req;
}
export interface AA0234Req {
    apiKey: string;
    moduleName: string;
    keyword: string;
    selectedApiKeyList: Array<string>;
}

export interface ResAA0234 extends BaseRes {
    RespBody: AA0234Resp;
}
export interface AA0234Resp {
    apiList: Array<Object>;
}
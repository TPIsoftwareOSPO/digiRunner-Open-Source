import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0235: 查詢群組所擁有的Module
 */
export interface ReqAA0233 extends BaseReq {
    ReqBody: AA0233Req;
}
export interface AA0233Req {
    moduleName: string;
    keyword: string;
    selectedModuleNameList: Array<string>;
}

export interface ResAA0233 extends BaseRes {
    RespBody: AA0233Resp;
}
export interface AA0233Resp {
    moduleNameList: Array<String>;
}
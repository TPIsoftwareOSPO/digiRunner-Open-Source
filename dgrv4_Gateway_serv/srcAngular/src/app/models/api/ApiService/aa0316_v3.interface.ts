import { ValidatorFormat } from "../../validator.interface";
import { BaseReq, BaseRes } from "../base.interface";

/**
 * AA0316: 註冊外部API List
 * 在TSMP中註冊外部既有的Http API清單。
 * 若有重複的apiKey+moduleName，不論在清單中或是已在DB中，則全部視為失敗，回覆錯誤。
 */

export interface ReqAA0316_v3 extends BaseReq {
    ReqBody: AA0316Req;
}
export interface AA0316Req {
    apiSrc: string;
    moduleSrc: string;
    tempFileName: string;
    moduleName: string;
    moduleVersion: string;
    regHostId?: string;
    regApiList: Array<AA0316Item>;
    type?:string;
    targetUrl:string;
}
export interface AA0316Item {
    methods: Array<string>; // Http Method
    srcUrl: string; // 完整的來源URL
    apiId: string; // API ID (原 API Key)
    dataFormat: string; // 資料格式
    apiDesc?: string; // API說明
    jweFlag: string; // JWT設定(Request)
    jweFlagResp: string; // JWT設定(Response)
    urlRID: boolean; // Path Parameter
    noOAuth: boolean; // No OAuth
    funFlag: AA0316Func; // 功能設定
    consumes?: Array<string>; // Request Content Type
    produces?: Array<string>; // Response Cotent Type
    headers?: Array<string>; // Http Headers
    params?: Array<string>; // Parameters
    moduleName?: string;
    summary?: string;
}
export interface AA0316Func {
    tokenPayload: boolean;
}

export interface ResAA0316_v3Before extends BaseRes {
    RespBody: AA0316RespBefore;
}
export interface AA0316RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0316_v3 extends BaseRes {
    RespBody: AA0316Resp;
}
export interface AA0316Resp {
    regModuleId?: number;
}

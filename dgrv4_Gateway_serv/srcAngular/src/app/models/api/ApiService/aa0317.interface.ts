import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0317: 匯出RegCompAPIs
 * 在TSMP Admin中匯出Registered & Composed APIs。
 * 此API匯出時自動乎略Registered & Composed以外的APIs。
 * 匯出時，Res_0317可另存新檔。
 */
export interface ReqAA0317 extends BaseReq {
    ReqBody: AA0317Req;
}
export interface AA0317Req {
    apiList: Array<AA0317ReqItem>;
}
export interface AA0317ReqItem {
    apiKey: string;
    moduleName: string;
}

export interface ResAA0317 extends BaseRes {
    RespBody: AA0317Resp;
}
export interface AA0317Resp {
    fileName: string;
    data: AA0317Data;
}
export interface AA0317Data {
    R?: Array<AA0317Module>;
    C?: Array<AA0317Module>;
}
export interface AA0317Module {
    moduleName: string;
    regModule?: AA0317RegModule;
    apiList: Array<AA0317RespItem>;
}
export interface AA0317RegModule {
    regModuleId: number;
    moduleName: string;
    moduleVersion: string;
    moduleSrc: string;
}
export interface AA0317RespItem {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiDesc?: string;
    apiOwner?: string
    urlRID: string;
    apiSrc: string;
    srcURL?: string;
    apiUUID?: string;
    contentType?: string;
    enpoint: string;
    httpHeader?: string;
    httpMethod: string;
    params?: string;
    produce?: string;
    flow?: Array<string>;
    no_oauth?: string;
    // dataFormat?: string;
    jweFlag?: string; // 0：不使用, 1：JWE, 2：JWS, null:不使用
    jweFlagResp?: string; // 0：不使用, 1：JWE, 2：JWS, null:不使用
    funFlag?: number;
}

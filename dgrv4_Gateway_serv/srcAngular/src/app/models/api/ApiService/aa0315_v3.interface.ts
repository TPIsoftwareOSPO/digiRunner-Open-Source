import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0315: 上傳OpenApiDoc
 * 上傳要註冊的OpenApi Doc檔案。OpenApi Doc檔可以是json或yaml。
 */
export interface ReqAA0315_v3 extends BaseReq {
    ReqBody: AA0315Req;
}
export interface AA0315Req {
    tempFileName: string;
    optionHost?:string;
    type?:string;
}

export interface ResAA0315_v3 extends BaseRes {
    RespBody: AA0315Resp;
}
export interface AA0315Resp {
    protocol?: string;
    host: string;
    basePath: string;
    moduleSrc: string;
    moduleName: string;
    moduleVersion: string;
    openApiList: Array<AA0315Item>;
    type?:string;
}
export interface AA0315Item {
    summary: string;
    rearPath: string;
    path: string;
    srcUrl: string;
    apiDesc?: string;
    methods: Array<string>;
    headers?: Array<string>;
    params?: Array<string>;
    consumes?: Array<string>;
    produces?: Array<string>;
    duplicateFlag?: Boolean;
    moduleName:string;
}




import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0303: 更新APIStatusList
 * 更新API的狀態。
 */
export interface ReqAA0303 extends BaseReq {
    ReqBody: AA0303Req;
}
export interface AA0303Req {
    ignoreAlert: string; // "Y"=是, "N"=否, 前端只有 [刪除] 時才會傳入 "N"
    apiList: Array<AA0303Item>;
    apiStatus?: string; // 0:Delete(真刪除，非改狀態) 1: Enabled, 2:Disabled
    jweFlag?: string; // 0:不使用, 1:JWE, 2:JWS
    jweFlagResp?: string; // 0:不使用, 1:JWE, 2:JWS
    scheduledDate?: Number;
}
export interface AA0303Item {
    apiKey: string;
    moduleName: string;
}

export interface ResAA0303 extends BaseRes {
    RespBody: AA0303Resp;
}
export interface AA0303Resp {
    msg?: string;
}

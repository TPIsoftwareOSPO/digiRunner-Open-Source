import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0304: 更新APIInfo
 * 可更新API的名稱，說明與狀態。
 */
export interface ReqAA0304 extends BaseReq {
    ReqBody: AA0304Req;
}
export interface AA0304Req {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiStatus: string;
    jweFlag: string; // 0：不使用、1：JWE、2：JWS、null:不使用
    jweFlagResp: string; // 0：不使用、1：JWE、2：JWS、null：不使用
    apiDesc?: string;
}

export interface ResAA0304Before extends BaseRes {
    RespBody: AA0304RespBefore;
}
export interface AA0304RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0304 extends BaseRes { }
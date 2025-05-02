import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0312: 測試外部API
 * 在TSMP中測試外部既有的Http API。
 */
export interface ReqAA0312 extends BaseReq {
    ReqBody: AA0312Req;
}
export interface AA0312Req {
    testURL: string;
    method: string;
    headerList?: Array<object>; // List<Map<String, String>>，List<Map<鍵, 值>> 請求表頭
    paramList?: Array<object>; // List<Map<String, String>>，List<Map<鍵, 值>> 請求表身(表單)
    bodyText?: string;
}
// 前端自用
export interface HttpHeader {
    name: string;
    value: string;
}
export interface FormParam {
    name: string;
    value: string;
}

export interface ResAA0312Before extends BaseRes {
    RespBody: AA0312RespBefore;
}
export interface AA0312RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0312 extends BaseRes {
    RespBody: AA0312Resp;
}
export interface AA0312Resp {
    headerList?: Array<object>; // List<Map<String, List<String>>>，ex: [ {"key1": ["a", "b"], "key2": [...]}, {...} ]
    resBody?: string;
    resStatus: number;
}



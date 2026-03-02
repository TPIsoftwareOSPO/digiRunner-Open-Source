import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0101: 查詢TFuncList
 * 查詢TSMP Admin的功能清單。TSMP Admin的功能簡稱TFunc。
 */
export interface ReqAA0101 extends BaseReq { }

export interface ResAA0101 extends BaseRes {
    RespBody: AA0101Resp;
}
export interface AA0101Resp {
    funcList: Array<AA0101func>;
}
export interface AA0101func {
    funcCode: string;
    funcName: string;
    funcURL: string;
}
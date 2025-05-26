import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0301: 查詢APIList
 * 查詢所有部署在TSMP中的API。預設查詢TSMP_API_LIST Table中所有API。
 */
export interface ReqAA0301_v3 extends BaseReq {
    ReqBody: AA0301Req;
}
export interface AA0301Req {
    moduleName?: string;
    apiKey?: string;
    keyword?: string;
    apiSrc?: Array<string>;
    apiStatus?: string;
    publicFlag?: string // bcrypt加密，ITEM_NO = 'API_AUTHORITY'
    jweFlag?: string; // bcrypt加密，ITEM_NO = 'API_JWT_FLAG'
    jweFlagResp?: string; // bcrypt加密，ITEM_NO = 'API_JWT_FLAG'
    sortBy?: Array<object>; // 前端不使用，Map<欄位名稱, 排序方向>
    paging?: string; // 前端固定傳 'Y'
}

export interface ResAA0301_v3 extends BaseRes {
    RespBody: AA0301Resp;
}
export interface AA0301Resp {
    sortBy: Array<object>;
    dataList: Array<AA0301Item>;
}
export interface AA0301Item {
    moduleName: AA0301Trunc;
    apiKey: AA0301Trunc;
    apiStatus: AA0301Pair; // 0:delete、1:enabled、2:disabled
    apiSrc: AA0301Pair;
    apiName: AA0301Trunc;
    apiDesc: AA0301Trunc;
    jweFlag: AA0301Pair; // 0:not use、1:JWE、2:JWS、null:not use
    jweFlagResp: AA0301Pair; // 0:not use、1:JWE、2:JWS、null:not use
    updateTime?: string;
    org?: AA0301Pair;
    enableScheduledDate: number;
    disableScheduledDate: number;
}
export interface AA0301Trunc {
    val: string; // 顯示用
    t: boolean; // 是否被截斷
    ori?: string; // 原始值，被截斷才回傳
}
export interface AA0301Pair {
    v: string;
    n?: string; // 顯示用
}

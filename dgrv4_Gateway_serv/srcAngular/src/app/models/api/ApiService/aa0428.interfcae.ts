import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0428: 查詢APIList
 * 查詢所有部署在TSMP中的API。預設查詢TSMP_API_LIST Table中所有API。
 */
export interface ReqAA0428 extends BaseReq {
  ReqBody: AA0428Req;
}
export interface AA0428Req {
  labelList: Array<string>;
  moduleName?: string;
  apiKey?: string;
  paging?: string; // 前端固定傳 'Y'
}

export interface ResAA0428 extends BaseRes {
  RespBody: AA0428Resp;
}
export interface AA0428Resp {
  sortBy: Array<object>;
  dataList: Array<AA0428Item>;
}
export interface AA0428Item {
  moduleName: AA0428Trunc;
  apiKey: AA0428Trunc;
  apiStatus: AA0428Pair; // 0:delete、1:enabled、2:disabled
  apiSrc: AA0428Pair;
  apiName: AA0428Trunc;
  apiDesc: AA0428Trunc;
  jweFlag: AA0428Pair; // 0:not use、1:JWE、2:JWS、null:not use
  jweFlagResp: AA0428Pair; // 0:not use、1:JWE、2:JWS、null:not use
  updateTime?: string;
  org?: AA0428Pair;
  labelList: Array<string>;
  enableScheduledDate: number;
  disableScheduledDate: number;
  createDate: string;
  createUser: string;
  updateDate?: string;
  updateUser?: string;
  apiCacheFlag: AA0428Pair;
  methodOfJson?: string;
  noAuth: string;
}
export interface AA0428Trunc {
  val: string; // 顯示用
  t: boolean; // 是否被截斷
  ori?: string; // 原始值，被截斷才回傳
}
export interface AA0428Pair {
  v: string;
  n?: string; // 顯示用
}

import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0318 extends BaseReq {
  ReqBody: AA0318Req;
}
export interface AA0318Req {
  tempFileName: string;
}

export interface ResAA0318 extends BaseRes {
  RespBody: AA0318Resp;
}
export interface AA0318Resp {
  batchNo: number;
  apiList: Array<AA0318Item>;
}
export interface AA0318Item {
  apiKey: AA0318Trunc;
  moduleName: AA0318Trunc;
  apiName: AA0318Trunc;
  apiSrc: AA0318Pair;
  endpoint: string;
  checkAct: AA0318Pair;
  memo?: AA0318Trunc;
  srcURLByIpRedirectMap?: string | Array<AA0318srcURLByIpRedirectItem>;
  failDiscoveryPolicy: string;
  failHandlePolicy: string;
  srcURL: string;
  isCorsAllowOrigin?: string;
  isCorsAllowMethods?: string;
  isCorsAllowHeaders?: string;
  corsAllowOrigin?: string;
  corsAllowMethods?: string;
  corsAllowHeaders?: string;
}

export interface AA0318srcURLByIpRedirectItem {
  ip: string;
  srcURL: string;
}

export interface AA0318Trunc {
  val: string;
  t: boolean;
  ori?: string;
}
export interface AA0318Pair {
  v: string;
  n?: string;
}

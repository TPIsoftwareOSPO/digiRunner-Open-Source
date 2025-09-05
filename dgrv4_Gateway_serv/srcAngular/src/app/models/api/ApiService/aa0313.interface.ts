import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0313 extends BaseReq {
  ReqBody: AA0313Req;
}
export interface AA0313Req {
  apiKey: string;
  moduleName: string;
  apiName: string;
  apiStatus: string;
  jweFlag: string; // 0：不使用、1：JWE、2：JWS、null:不使用
  jweFlagResp: string; // 0：不使用、1：JWE、2：JWS、null：不使用
  protocol?: string; // 可以是 "https" 或 "http" (apiSrc = "C" 時，前端不會傳入值)
  srcUrl: string;
  urlRID: boolean;
  noOAuth: boolean;
  funFlag: AA0313Func;
  methodOfJson?: Array<string>;
  dataFormat: string;
  reghostId?: string;
  apiDesc?: string;
  apiCacheFlag: string;
  mockStatusCode?: string;
  mockHeaders?: Array<AA0313KeyVal>;
  mockBody?: string;
  redirectByIp: boolean;
  redirectByIpDataList?: Array<AA0313RediretByIpData>; //  redirectByIp ? true => 1~5
  headerMaskPolicy: string; //0,1,2,3
  headerMaskPolicyNum?: number; // 1-9999
  headerMaskPolicySymbol?: string; // char length 1~10
  headerMaskKey?: string; //xxx,xxxx,xx
  bodyMaskPolicy: string;
  bodyMaskPolicyNum?: number;
  bodyMaskPolicySymbol?: string;
  bodyMaskKeyword?: string;
  labelList?: Array<string>;
  fixedCacheTime?: number;
  failDiscoveryPolicy?: string;
  failHandlePolicy?: string;
  notifyNameList?: Array<string>;
  isCorsAllowOrigin?: string;
  isCorsAllowMethods?: string;
  isCorsAllowHeaders?: string;
  corsAllowOrigin?: string;
  corsAllowMethods?: string;
  corsAllowHeaders?: string;
}
// 要改為 extends AA0316Func
export interface AA0313Func {
  tokenPayload: boolean;
}

export interface AA0313RediretByIpData {
  ipForRedirect: string;
  ipSrcUrl: string;
}

export interface ResAA0313Before extends BaseRes {
  RespBody: AA0313RespBefore;
}
export interface AA0313RespBefore {
  constraints: Array<ValidatorFormat>;
}
export interface AA0313KeyVal {
  key: string;
  value: string;
}
export interface ResAA0313 extends BaseRes {}

import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0302_v3 extends BaseReq {
  ReqBody: AA0302Req;
}
export interface AA0302Req {
  orgId: string;
  moduleName: string;
  apiKey: string;
}

export interface ResAA0302_v3 extends BaseRes {
  RespBody: AA0302Resp;
}
export interface AA0302Resp {
  apiStatus: AA0302Pair;
  moduleId: number;
  moduleName: AA0302Trunc;
  apiKey: AA0302Trunc;
  apiName: AA0302Trunc;
  apiSrc: AA0302Pair;
  jweFlag: AA0302Pair; // 0：不使用、1：JWE、2：JWS、null：不使用
  jweFlagResp: AA0302Pair; // 0：不使用、1：JWE、2：JWS、null：不使用
  methodOfJson?: string; // Http Method
  pathOfJson?: AA0302Trunc; // 端點
  protocol?: string;
  srcUrl?: AA0302Trunc;
  urlRID: string; // path parameter
  noOAuth: string;
  funFlag: number;
  dataFormat: AA0302Pair;
  regHostId?: string;
  regHostName?: string;
  headersOfJson?: AA0302Trunc;
  paramsOfJson?: AA0302Trunc;
  consumesOfJson?: AA0302Trunc;
  producesOfJson?: AA0302Trunc;
  apiDesc: string;
  apiUUID: string;
  orgId?: string;
  orgName: string;
  controls: AA0302Controls;
  apiCacheFlag: AA0302Pair;
  pathType: string;
  dgrPath: string;
  mockStatusCode?: string;
  mockHeaders?: Array<AA0313KeyVal>;
  mockBody?: string;
  mockHeadersOfJson?: string;
  headerMaskPolicy: string;
  headerMaskPolicyNum?: number;
  headerMaskPolicySymbol?: string;
  headerMaskKey?: string;
  bodyMaskPolicy: string;
  bodyMaskPolicyNum?: number;
  bodyMaskPolicySymbol?: string;
  bodyMaskKeyword?: string;
  isRedirectByIp: boolean;
  redirectByIpDataList: Array<AA0302RedirectByIpData>;
  labelList: Array<string>;
  fixedCacheTime: number;
  type?: string;
  failDiscoveryPolicy: string;
  failHandlePolicy: string;
  enableScheduledDate: string;
  disableScheduledDate: string;
  createDate: string;
  createUser: string;
  updateDate?: string;
  updateUser?: string;
}
export interface AA0302RedirectByIpData {
  ipForRedirect: string;
  ipSrcUrl: AA0302Trunc;
}
export interface AA0302Trunc {
  v: string; // 顯示用
  t: boolean; // 是否被截斷
  o?: string; // 原始值，被截斷才回傳
}
export interface AA0302Pair {
  v: string;
  n?: string; // 顯示用
}
export interface AA0302Controls {
  apiName: boolean;
  protocol: boolean;
  srcUrl: boolean;
  methodOfJson: boolean;
  dataFormat: boolean;
  regHostId: boolean;
}
export interface AA0313KeyVal {
  key: string;
  value: string;
}

import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from "../../validator.interface";

/**
 * AA0311: 註冊外部API
 * 在TSMP中註冊外部既有的Http API。
 */
export interface ReqAA0311_v3 extends BaseReq {
  ReqBody: AA0311Req;
}
export interface AA0311Req {
  apiSrc: string;
  protocol?: string;
  srcUrl: string;
  moduleName: string;
  apiId: string;
  urlRID: Boolean;
  noOAuth: Boolean;
  funFlag: AA0311Func;
  methods: Array<string>;
  dataFormat?: string;
  regHostId?: string;
  jweFlag: string;
  jweFlagResp: string;
  apiDesc?: string;
  consumes?: Array<string>;
  produces?: Array<string>;
  headers?: Array<string>;
  params?: Array<string>;
  apiName?: string;
  type?: number; // 0:tsmpc, 1:dgrc
  redirectByIp: boolean;
  redirectByIpDataList?: Array<AA0311RedirectByIpData>  //  redirectByIp ? true => 1~5
  headerMaskPolicy: string; //0,1,2,3
  headerMaskPolicyNum?: number; // 1-9999
  headerMaskPolicySymbol?: string; // char length 1~10
  headerMaskKey?: string; //xxx,xxxx,xx
  bodyMaskPolicy: string;
  bodyMaskPolicyNum?: number;
  bodyMaskPolicySymbol?: string;
  bodyMaskKeyword?: string;
  labelList?: Array<string>;
  failDiscoveryPolicy?: string;
  failHandlePolicy?: string;
  apiType?:string;
  notifyNameList?:Array<string>;
}
export interface AA0311Func {
  tokenPayload: Boolean;
}

export interface AA0311RedirectByIpData {
  ipForRedirect: string;
  ipSrcUrl: string;
}

export interface ResAA0311_v3Before extends BaseRes {
  RespBody: AA0311RespBefore;
}
export interface AA0311RespBefore {
  constraints: Array<ValidatorFormat>;
}
export interface ResAA0311_v3 extends BaseRes { }

import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0312 extends BaseReq {
  ReqBody: DPB0312Req;
}

export interface DPB0312Req {
  proxyList: Array<SmartOnFhirProxyUpdateDto>;
}

export interface SmartOnFhirProxyUpdateDto {
  sofProxyId: string;
  version: string;

  sofProxyName?: string;
  sofProxyStatus?: string;
  sofProxyRemark?: string;
  sofProxyAccessToken?: string;
  sofProxySqlInjection?: string;
  sofProxyTraffic?: string;
  sofProxyXss?: string;
  sofProxyXxe?: string;
  sofProxyShowLog?: string;
  sofProxyTps?: number;
  sofProxyIgnoreApi?: string;
  sofProxyClientId?: Array<string>;

  diversionList?: Array<SmartOnFhirProxyDiversionUpdateDto>;
}

export interface RespDPB0312 extends BaseRes {
  RespBody: DPB0312Resp;
}

export interface DPB0312Resp {
  successCount: number;
  updatedProxies: Array<SmartOnFhirProxyDto>;
}

export interface SmartOnFhirProxyDiversionUpdateDto {
  sofProxyDiversionId?: string;
  version?: string;
  sofProxyDiversionProbability: number;
  sofProxyDiversionUrl: string;
}

export interface SmartOnFhirProxyDto {
  sofProxyId: string;
  sofProxyName: string;
  sofProxyStatus: string;
  sofProxyStatusName: string;
  sofProxyRemark: string;
  sofProxyAccessToken: string;
  sofProxySqlInjection: string;
  sofProxyTraffic: string;
  sofProxyXss: string;
  sofProxyXxe: string;
  sofProxyTps: number;
  sofProxyIgnoreApi: string;
  sofProxyClientId: Array<string>;
  sofProxyShowLog: string;
  diversionList: Array<SmartOnFhirProxyDiversionDto>;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
  version: string;
}

export interface SmartOnFhirProxyDiversionDto {
  sofProxyDiversionId: string;
  sofProxyId: string;
  sofProxyDiversionProbability: number;
  sofProxyDiversionUrl: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
  version: string;
}

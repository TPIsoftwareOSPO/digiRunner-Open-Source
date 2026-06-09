import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0310 extends BaseReq {
  ReqBody: SmartOnFhirProxySearchReq;
}

export interface SmartOnFhirProxySearchReq {
  pageNum?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: string;
  keywords?: Array<string>;
  sofProxyStatus?: string;
}

export interface RespDPB0310 extends BaseRes {
  RespBody: DPB0310Resp;
}

export interface DPB0310Resp {
  content: Array<SmartOnFhirProxyDto>;
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface SmartOnFhirProxyDto {
  sofProxyId?: string;
  sofProxyName?: string;
  sofProxyStatus?: string;
  sofProxyStatusName?: string;
  sofProxyRemark?: string;
  sofProxyAccessToken?: string;
  sofProxySqlInjection?: string;
  sofProxyTraffic?: string;
  sofProxyXss?: string;
  sofProxyXxe?: string;
  sofProxyTps?: number;
  sofProxyIgnoreApi?: string;
  sofProxyClientId?: Array<string>;
  sofProxyShowLog?: string;
  sofProxySticky?:string;
  diversionList?: Array<SmartOnFhirProxyDiversionDto>;
  createDateTime?: string | number;
  createUser?: string;
  updateDateTime?: string | number;
  updateUser?: string;
  version?: string;
}

export interface SmartOnFhirProxyDiversionDto {
  sofProxyDiversionId: string;
  sofProxyId: string;
  sofProxyDiversionProbability: number;
  sofProxyDiversionUrl: string;
  createDateTime: string | number;
  createUser: string;
  updateDateTime: string | number;
  updateUser: string;
  version: string;
}

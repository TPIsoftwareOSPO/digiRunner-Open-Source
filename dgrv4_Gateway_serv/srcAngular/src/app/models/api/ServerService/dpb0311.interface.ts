import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0311 extends BaseReq {
  ReqBody: SmartOnFhirProxyDto;
}

export interface SmartOnFhirProxyDto {
  sofProxyId?: string;
  sofProxyName: string;
  sofProxyStatus: string;
  sofProxyStatusName?: string;
  sofProxyRemark: string;
  sofProxyAccessToken: string;
  sofProxyAuth?: Array<string>;
  sofProxyInIntrospectionEndpoint?: string;
  sofProxyInWellKnown?: string;
  sofProxyInIntrospectionAuth?: string;
  sofProxyInIntrospectionClientId?: string;
  hasIntrospectionSecret?: boolean;
  sofProxyInIntrospectionSecret?: string;
  sofProxyInAllowedIss?: Array<string>;
  sofProxyInExpectedAud?: string;
  sofProxyOutRelay?: string;
  sofProxyOutAuthMethod?: string;
  sofProxyOutClientId?: string;
  hasOutClientSecret?: boolean;
  sofProxyOutClientSecret?: string;
  sofProxyOutWellKnown?: string;
  sofProxyOutTokenEndpoint?: string;
  sofProxyOutScope?: string;
  sofProxySqlInjection: string;
  sofProxyTraffic: string;
  sofProxyXss: string;
  sofProxyXxe: string;
  sofProxyTps: number;
  sofProxyIgnoreApi: string;
  sofProxyClientId: Array<string>;
  sofProxyShowLog: string;
  sofProxySticky: string;
  diversionList: Array<SmartOnFhirProxyDiversionDto>;
  createDateTime?: string | number;
  createUser?: string;
  updateDateTime?: string | number;
  updateUser?: string;
  version?: string;
}

export interface RespDPB0311 extends BaseRes {
  RespBody: SmartOnFhirProxyDto;
}

export interface SmartOnFhirProxyDiversionDto {
  sofProxyDiversionId?: string;
  sofProxyId?: string;
  sofProxyDiversionProbability?: number;
  sofProxyDiversionUrl?: string;
  createDateTime?: string | number;
  createUser?: string;
  updateDateTime?: string | number;
  updateUser?: string;
  version?: string;
}

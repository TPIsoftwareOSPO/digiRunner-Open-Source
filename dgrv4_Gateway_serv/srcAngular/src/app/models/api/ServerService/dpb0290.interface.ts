import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0290 extends BaseReq {
  ReqBody: DPB0290Req;
}

export interface DPB0290Req {
  serviceName: string;
  proxyHostName: string;
  targetHostName: string;
  targetPort: string;
  connectTimeoutMs: string;
  sendTimeoutMs: string;
  readTimeoutMs: string;
  secureMode: string;
  serverCertContent?: string;
  serverKeyContent?: string;
  autoTrustUpstreamCerts: string;
  trustedCertsContent?: string;
  enable: string;  
}

export interface RespDPB0290 extends BaseRes {
  RespBody: DPB0290Resp;
}

export interface DPB0290Resp {
  gRPCProxyMapId: string;
}

export interface RespDPB0290Before extends BaseRes {
  RespBody: RespDPB0290RespBefore;
}

export interface RespDPB0290RespBefore {
  constraints: Array<ValidatorFormat>;
}

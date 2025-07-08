import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0292 extends BaseReq {
  ReqBody: DPB0292Req;
}

export interface DPB0292Req {
  gRPCProxyMapId: string;
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

export interface RespDPB0292 extends BaseRes {
  RespBody: DPB0292Resp;
}

export interface DPB0292Resp {
  gRPCProxyMapId: string;
  serviceName: string;
  proxyHostName: string;
  targetHostName: string;
  targetPort: string;
  connectTimeoutMs: string;
  sendTimeoutMs: string;
  readTimeoutMs: string;
  enable: string;
}

export interface RespDPB0292Before extends BaseRes {
  RespBody: RespDPB0292RespBefore;
}

export interface RespDPB0292RespBefore {
  constraints: Array<ValidatorFormat>;
}

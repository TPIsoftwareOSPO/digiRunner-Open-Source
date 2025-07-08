import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0294 extends BaseReq {
  ReqBody: DPB0294Req;
}

export interface DPB0294Req {}

export interface RespDPB0294 extends BaseRes {
  RespBody: DPB0294Resp;
}

export interface DPB0294Resp {
  infoList: Array<DPB0294RespItem>;
}
export interface DPB0294RespItem {
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

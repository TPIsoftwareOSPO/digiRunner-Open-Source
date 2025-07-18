import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0297 extends BaseReq {
  ReqBody: Array<DgrGrpcProxyMapDto>;
}

export interface RespDPB0297 extends BaseRes {
  RespBody: Array<DgrGrpcProxyMapDto>;
}

export interface DgrGrpcProxyMapDto {
  grpcproxyMapId: string;
  serviceName: string;
  proxyHostName: string;
  targetHostName: string;
  targetPort: string;
  connectTimeoutMs: string;
  sendTimeoutMs: string;
  readTimeoutMs: string;
  secureMode: string;
  serverCertContent: string;
  serverKeyContent: string;
  autoTrustUpstreamCerts: string;
  trustedCertsContent: string;
  enable: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
  version: string;
  create: boolean;
  update: boolean;
  success: boolean;
  errorMessage: string;
  note: string;
  uuid:string;
}

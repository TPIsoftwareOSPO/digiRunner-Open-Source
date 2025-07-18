import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0296 extends BaseReq {
  ReqBody: File;
}

export interface RespDPB0296 extends BaseRes {
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

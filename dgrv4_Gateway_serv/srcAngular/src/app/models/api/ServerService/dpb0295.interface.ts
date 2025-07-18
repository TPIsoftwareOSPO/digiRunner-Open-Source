import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0295 extends BaseReq {
  ReqBody: Array<string>;
}

export interface RespDPB0295 extends BaseRes {
  RespBody: DPB0295Resp;
}

export interface DPB0295Resp {
  fileName: string;
  data: Array<DgrGrpcProxyMapDto>;
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

import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0300 extends BaseReq {
  ReqBody: DPB0300Req;
}

export interface DPB0300Req {
  targetHostName: string;
  targetPort: string;
  connectTimeoutMs: string;
  secureMode: string;
  autoTrustUpstreamCerts: string;
  trustedCertsContent?: string;
}

export interface RespDPB0300 extends BaseRes {
  RespBody: DPB0300Resp;
}

export interface DPB0300Resp {
  isConn: boolean;
  errMsg?: string;
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0226 extends BaseReq {
  ReqBody: DPB0226Req;
}

export interface DPB0226Req {
  dgrMtlsClientCertId: string;
  host: string;
  port: string;
  rootCa: string;
  clientCert?: string;
  clientKey: string;
  keyMima?: string;
  remark?: string;
}

export interface RespDPB0226 extends BaseRes {
  RespBody: DPB0226Resp;
}

export interface DPB0226Resp {}

export interface DPB0226RespBefore extends BaseRes {
  RespBody: RespDPB0226RespBefore;
}

export interface RespDPB0226RespBefore {
  constraints: Array<ValidatorFormat>;
}

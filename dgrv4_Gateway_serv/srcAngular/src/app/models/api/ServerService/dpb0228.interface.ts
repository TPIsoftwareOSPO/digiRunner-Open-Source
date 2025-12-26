import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0228 extends BaseReq {
  ReqBody: DPB0228Req;
}

export interface DPB0228Req {
  dgrMtlsClientCertId: string;
}

export interface RespDPB0228 extends BaseRes {
  RespBody: DPB0228Resp;
}

export interface DPB0228Resp {
  dgrMtlsClientCertId: string;
  host: string;
  port: string;
  rootCa: string;
  clientCert: string;
  clientKey: string;
  keyMima: string;
  remark: string;
  enable: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}

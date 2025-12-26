import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0227 extends BaseReq {
  ReqBody: DPB0227Req;
}

export interface DPB0227Req {
  dgrMtlsClientCertId:string;
}

export interface RespDPB0227 extends BaseRes {
  RespBody: DPB0227Resp;
}

export interface DPB0227Resp { }

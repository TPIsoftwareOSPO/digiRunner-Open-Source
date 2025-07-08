import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0293 extends BaseReq {
  ReqBody: DPB0293Req;
}

export interface DPB0293Req {
  gRPCProxyMapId: string;
}

export interface RespDPB0293 extends BaseRes {
  RespBody: DPB0293Resp;
}

export interface DPB0293Resp { }
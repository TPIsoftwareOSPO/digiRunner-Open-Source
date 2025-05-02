import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0200 extends BaseReq {
  ReqBody: DPB0200Req;
}

export interface DPB0200Req {
  jdbcUrl: string;
  userName: string;
  mima?: string;
  connName?: string;
}

export interface RespDPB0200 extends BaseRes {
  RespBody: DPB0200Resp;
}

export interface DPB0200Resp {
  msg: string;
  success: string;
}

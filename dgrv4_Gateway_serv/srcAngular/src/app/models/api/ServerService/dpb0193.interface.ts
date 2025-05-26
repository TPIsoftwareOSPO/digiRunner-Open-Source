import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0193 extends BaseReq {
  ReqBody: DPB0193Req;
}

export interface DPB0193Req {
  connectionName: string;
  jdbcUrl: string;
  userName: string;
  mima?: string;
  maxPoolSize: string;
  connectionTimeout: string;
  idleTimeout: string;
  maxLifetime: string;
  dataSourceProperty?: string;
}

export interface RespDPB0193 extends BaseRes {
  RespBody: DPB0193Resp;
}

export interface DPB0193Resp { }

export interface RespDPB0193Before extends BaseRes {
  RespBody: RespDPB0193RespBefore;
}

export interface RespDPB0193RespBefore {
  constraints: Array<ValidatorFormat>;
}

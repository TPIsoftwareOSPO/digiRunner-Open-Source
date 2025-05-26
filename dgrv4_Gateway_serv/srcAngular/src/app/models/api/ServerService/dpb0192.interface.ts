import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0192 extends BaseReq {
  ReqBody: DPB0192Req;
}

export interface DPB0192Req {
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

export interface RespDPB0192 extends BaseRes {
  RespBody: DPB0192Resp;
}

export interface DPB0192Resp { }

export interface RespDPB0192Before extends BaseRes {
  RespBody: RespDPB0192RespBefore;
}

export interface RespDPB0192RespBefore {
  constraints: Array<ValidatorFormat>;
}

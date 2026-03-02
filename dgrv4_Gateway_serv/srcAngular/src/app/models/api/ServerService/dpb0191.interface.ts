import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0191 extends BaseReq {
  ReqBody: DPB0191Req;
}

export interface DPB0191Req {
  connectionName: string;
}

export interface RespDPB0191 extends BaseRes {
  RespBody: DPB0191Resp;
}

export interface DPB0191Resp {
  connectionName: string;
  jdbcUrl: string;
  userName: string;
  mima: string;
  maxPoolSize: string;
  connectionTimeout: string;
  idleTimeout: string;
  maxLifetime: string;
  dataSourceProperty?: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}


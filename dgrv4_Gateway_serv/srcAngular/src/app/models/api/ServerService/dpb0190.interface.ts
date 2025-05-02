import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0190 extends BaseReq {
  ReqBody: DPB0190Req;
}

export interface DPB0190Req { }

export interface RespDPB0190 extends BaseRes {
  RespBody: DPB0190Resp;
}

export interface DPB0190Resp {
  infoList: Array<DPB0190RespItem>;
}

export interface DPB0190RespItem {
  connectionName: string;
  jdbcUrl: string;
  maxPoolSize: string;
  updateDateTime: string;
  updateUser: string;
}


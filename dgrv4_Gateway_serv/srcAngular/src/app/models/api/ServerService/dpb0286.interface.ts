import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0286 extends BaseReq {
  ReqBody: DPB0286Req;
}

export interface DPB0286Req {
  webhookNotifyLogId:string;
}

export interface RespDPB0286 extends BaseRes {
  RespBody: DPB0286Resp;
}

export interface DPB0286Resp { }


import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0284 extends BaseReq {
  ReqBody: DPB0284Req;
}

export interface DPB0284Req {
  webhookNotifyId: string;  
}

export interface RespDPB0284 extends BaseRes {
  RespBody: DPB0284Resp;
}

export interface DPB0284Resp { }
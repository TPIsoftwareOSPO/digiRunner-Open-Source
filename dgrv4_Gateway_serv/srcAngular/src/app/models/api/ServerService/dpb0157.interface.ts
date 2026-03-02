import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0157 extends BaseReq {
  ReqBody: DPB0157Req;
}
export interface DPB0157Req {
  id: string;
}

export interface RespDPB0157 extends BaseRes {
  RespBody: DPB0157Resp;
}

export interface DPB0157Resp { }


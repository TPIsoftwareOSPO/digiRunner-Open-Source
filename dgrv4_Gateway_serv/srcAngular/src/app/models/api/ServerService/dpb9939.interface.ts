import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9939 extends BaseReq {
  ReqBody: DPB9939Req;
}

export interface DPB9939Req {}

export interface RespDPB9939 extends BaseRes {
  RespBody: DPB9939Resp;
}

export interface DPB9939Resp {
  connection: boolean;
  resp?: string;
}

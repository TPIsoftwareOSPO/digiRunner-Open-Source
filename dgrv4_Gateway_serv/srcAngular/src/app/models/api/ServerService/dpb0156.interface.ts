import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0156 extends BaseReq {
  ReqBody: DPB0156Req;
}
export interface DPB0156Req {
  dgrWebsiteId: string;
}

export interface RespDPB0156 extends BaseRes {
  RespBody: DPB0156Resp;
}

export interface DPB0156Resp { }


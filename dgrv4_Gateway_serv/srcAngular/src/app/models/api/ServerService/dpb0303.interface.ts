import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0303 extends BaseReq {
  ReqBody: DPB0303Req;
}

export interface DPB0303Req {}

export interface RespDPB0303 extends BaseRes {
  RespBody: DPB0303Resp;
}

export interface DPB0303Resp {
  sourceId: string;
  targetIds: string[];
}

export interface DPB0303RespItem {
  success: boolean;
  message: string;
  syncId: string;
}

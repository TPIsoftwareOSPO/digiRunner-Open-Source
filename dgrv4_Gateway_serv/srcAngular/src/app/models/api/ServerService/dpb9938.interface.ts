import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9938 extends BaseReq {
  ReqBody: DPB9938Req;
}

export interface DPB9938Req {}

export interface RespDPB9938 extends BaseRes {
  RespBody: DPB9938Resp;
}

export interface DPB9938Resp {
  esRespMap: Array<DPB9938EsResp>;
}

export interface DPB9938EsResp {
  connection: boolean;
  resp?: string;
}

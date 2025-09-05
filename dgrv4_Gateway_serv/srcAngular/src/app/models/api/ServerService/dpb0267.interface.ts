import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0267 extends BaseReq {
  ReqBody: DPB0267Req;
}

export interface DPB0267Req {
  aiApiKeyConsumerType: string;
  aiApiKeyConsumerId: string;
  aiPromptTemplateId: string;
}

export interface RespDPB0267 extends BaseRes {
  RespBody: DPB0267Resp;
}

export interface DPB0267Resp {}

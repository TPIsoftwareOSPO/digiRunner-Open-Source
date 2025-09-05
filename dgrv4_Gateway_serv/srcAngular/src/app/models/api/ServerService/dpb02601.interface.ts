import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB02601 extends BaseReq {
  ReqBody: DPB02601Req;
}

export interface DPB02601Req {}

export interface RespDPB02601 extends BaseRes {
  RespBody: DPB02601Resp;
}

export interface DPB02601Resp {
  aiApiKeyUsageId: string;
  aiApiKeyConsumerType: string;
  aiApiKeyConsuerId: string;
  aiApiKeyCode: string;
  requestTs: string;
  inputTokenCount: string;
  outputTokenCount: string;
  aiPromptTemplateId: string;
  aiUsagePromptContent: string;
  aiUsagePromptResponse: string;
  httpTransactionStatus: string;
}

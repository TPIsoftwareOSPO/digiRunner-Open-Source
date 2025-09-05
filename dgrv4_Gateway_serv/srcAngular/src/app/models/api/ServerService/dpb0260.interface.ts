import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0260 extends BaseReq {
  ReqBody: DPB0260Req;
}

export interface DPB0260Req {
  aiApiKeyConsumerType?: string;
  aiApiKeyConsumerId?: string;
  aiApiKeyCode?: string;
  aiPromptTemplateId?: string;
  pagination?: any;
}

export interface RespDPB0260 extends BaseRes {
  RespBody: DPB0260Resp;
}

export interface DPB0260Resp {
  content: Array<DPB0260RespItem>;
  totalElements: number;
  number: number;
}

export interface DPB0260RespItem {
  id: string;
  aiApiKeyUsageId: string;
  aiApikeyConsumerType: string;
  aiApikeyConsumerId: string;
  aiApiKeyConsuerId: string;
  aiApiKeyCode: string;
  requestTs: string;
  inputTokenCount: string;
  outputTokenCount: string;
  aiPromptTemplateId: string;
  httpTransactionStatus: string;
}

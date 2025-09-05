import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB02671 extends BaseReq {
  ReqBody: DPB02671Req;
}

export interface DPB02671Req {}

export interface RespDPB02671 extends BaseRes {
  RespBody: DPB02671Resp;
}

export interface DPB02671Resp {
  content: Array<DPB02671RespItem>;
}

export interface DPB02671RespItem {
  id: string;
  aiPromptTemplateId: string;
  aiApikeyConsumerType: string;
  aiApikeyConsumerId: string;
  dgrAiPromptTemplate: DPB02671dgrAiPromptTemplateItem;
}

export interface DPB02671dgrAiPromptTemplateItem {
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateName: string;
  aiPromptTemplateRemark: string;
  id: string;
}

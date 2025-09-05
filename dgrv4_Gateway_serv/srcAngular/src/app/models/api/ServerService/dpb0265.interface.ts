import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0265 extends BaseReq {
  ReqBody: DPB0265Req;
}

export interface DPB0265Req {
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark: string;
}

export interface RespDPB0265 extends BaseRes {
  RespBody: DPB0265Resp;
}

export interface DPB0265Resp {
  aiPromptTemplateid: string;
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark: string;
}

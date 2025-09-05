import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0263 extends BaseReq {
  ReqBody: DPB0263Req;
}

export interface DPB0263Req {
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark?: string;
}

export interface RespDPB0263 extends BaseRes {
  RespBody: DPB0263Resp;
}

export interface DPB0263Resp {
  aiPromptTemplateid: string;
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark: string;
}

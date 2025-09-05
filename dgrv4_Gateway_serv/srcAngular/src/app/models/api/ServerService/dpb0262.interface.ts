import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0262 extends BaseReq {
  ReqBody: DPB0262Req;
}

export interface DPB0262Req {
  aiPromptTemplateName?: string;
  aiPromptTemplateEnable?: string;
}

export interface RespDPB0262 extends BaseRes {
  RespBody: DPB0262Resp;
}

export interface DPB0262Resp {
  content: Array<DPB0262RespItem>;
}

export interface DPB0262RespItem {
  id: string;
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark: string;

}

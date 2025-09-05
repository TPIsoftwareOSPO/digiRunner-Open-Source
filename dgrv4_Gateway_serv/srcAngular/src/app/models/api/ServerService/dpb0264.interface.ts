import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0264 extends BaseReq {
  ReqBody: DPB0264Req;
}

export interface DPB0264Req { }

export interface RespDPB0264 extends BaseRes {
  RespBody: DPB0264Resp;
}

export interface DPB0264Resp {
  id: string;
  aiPromptTemplateName: string;
  aiPromptTemplateContent: string;
  aiPromptTemplateEnable: string;
  aiPromptTemplateRemark: string;
}

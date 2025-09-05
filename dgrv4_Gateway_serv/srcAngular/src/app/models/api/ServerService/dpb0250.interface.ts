import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0250 extends BaseReq {
  ReqBody: DPB0250Req;
}

export interface DPB0250Req {}

export interface RespDPB0250 extends BaseRes {
  RespBody: DPB0250Resp;
}

export interface DPB0250Resp {
  content: Array<contentItem>;
}

export interface contentItem {
  aiProviderId: string;
  aiProviderName: string;
  aiModel: string;
  aiProviderEnable: string;
  apiKeyCount: string;
  aiProviderAlias:string;
}
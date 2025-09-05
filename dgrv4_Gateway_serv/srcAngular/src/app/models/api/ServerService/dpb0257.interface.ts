import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0257 extends BaseReq {
  ReqBody: DPB0257Req;
}

export interface DPB0257Req {}

export interface RespDPB0257 extends BaseRes {
  RespBody: DPB0257Resp;
}

export interface DPB0257Resp {
  id: string;
  aiApikeyId: string;
  aiApikeyName: string;
  aiProviderId: string;
  aiApikeyCode: string;
  usageLimitInputToken: string;
  usageLimitOutputToken: string;
  usageLimitPolicy: string;
  backupKeyId: string;
  aiApikeyEnable: string;
  dgrAiProvider: dgrAiProviderItem;
}

export interface dgrAiProviderItem {
  aiModel: string;
  aiProviderAlias: string;
  aiProviderEnable: string;
  aiProviderId: string;
  aiProviderName: string;
  countTokenApi: string;
  createDateTime: number;
  createUser: string;
  generateApi: string;
  updateDateTime: number;
  updateUser: string;
}

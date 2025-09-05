import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0255 extends BaseReq {
  ReqBody: DPB0255Req;
}

export interface DPB0255Req {
  aiProviderId?: string;
  aiProviderName?: string;
  aiModel?: string;
}

export interface RespDPB0255 extends BaseRes {
  RespBody: DPB0255Resp;
}

export interface DPB0255Resp {
  content: Array<DPB0255RespItem>;
}

export interface DPB0255RespItem {
  id: string;
  aiApiKeyId: string;
  aiApiKeyName: string;
  aiProviderId: string;
  aiApiKeyCode: string;
  usageLimitInputToken: string;
  usageLimitOutputToken: string;
  usageLimitPolicy: string;
  backupKeyId: string;
  aiApikeyEnable: string;
}

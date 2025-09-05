import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0258 extends BaseReq {
  ReqBody: DPB0258Req;
}

export interface DPB0258Req {
  aiApiKeyName: string;
  aiProviderId: string;
  aiApiKeyCode: string;
  usageLimitInputToken: string;
  usageLimitOutputToken: string;
  usageLimitPolicy: string;
  backupKeyId: string;
  aiApiKeyEnable: string;
}

export interface RespDPB0258 extends BaseRes {
  RespBody: DPB0258Resp;
}

export interface DPB0258Resp {
  aiApiKeyId: string;
  aiApiKeyName: string;
  aiProviderId: string;
  aiApiKeyCode: string;
  usageLimitInputToken: string;
  usageLimitOutpuToken: string;
  usageLimitPolicy: string;
  backupKeyId: string;
  aiApiKeyEnable: string;
}


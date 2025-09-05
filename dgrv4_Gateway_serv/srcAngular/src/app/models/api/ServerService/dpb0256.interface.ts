import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0256 extends BaseReq {
  ReqBody: DPB0256Req;
}

export interface DPB0256Req {
  aiApiKeyName: string;
  aiProviderId: string;
  aiApiKeyCode: string;
  usageLimitInputToken: string;
  usageLimitOutputToken: string;
  usageLimitPolicy: string;
  backupKeyId: string;
  aiApiKeyEnable: string;
}

export interface RespDPB0256 extends BaseRes {
  RespBody: DPB0256Resp;
}

export interface DPB0256Resp {
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

// export interface RespDPB0256Before extends BaseRes {
//   RespBody: RespDPB0256RespBefore;
// }

// export interface RespDPB0256RespBefore {
//   constraints: Array<ValidatorFormat>;
// }

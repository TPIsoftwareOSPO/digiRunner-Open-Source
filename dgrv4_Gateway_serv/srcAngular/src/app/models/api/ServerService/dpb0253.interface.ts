import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0253 extends BaseReq {
  ReqBody: DPB0253Req;
}

export interface DPB0253Req {
  aiProviderName: string;
  aiProviderAlias: string;
  aiModel: string;
  generateAPI: string;
  countTokenAPI: string;
  aiProviderEnabled: string;
}

export interface RespDPB0253 extends BaseRes {
  RespBody: DPB0253Resp;
}

export interface DPB0253Resp {
  aiProviderId: string;
  aiProviderName: string;
  aiProviderAlias: string;
  aiModel: string;
  generateAPI: string;
  countTokenAPI: string;
  aiProviderEnabled: string;
}

export interface RespDPB0253Before extends BaseRes {
  RespBody: RespDPB0253RespBefore;
}

export interface RespDPB0253RespBefore {
  constraints: Array<ValidatorFormat>;
}

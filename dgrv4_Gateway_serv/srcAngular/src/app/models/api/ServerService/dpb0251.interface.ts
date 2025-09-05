import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0251 extends BaseReq {
  ReqBody: DPB0251Req;
}

export interface DPB0251Req {
  aiProviderName: string;
  aiProviderAlias: string;
  aiModel: string;
  generateAPI: string;
  countTokenAPI: string;
  aiProviderEnabled: string;
}

export interface RespDPB0251 extends BaseRes {
  RespBody: DPB0251Resp;
}

export interface DPB0251Resp {
  aiProviderId: string;
  aiProviderName: string;
  aiModel: string;
  generateAPI: string;
  countTokenAPI: string;
  aiProviderEnabled: string;
}

export interface RespDPB0251Before extends BaseRes {
  RespBody: RespDPB0251RespBefore;
}

export interface RespDPB0251RespBefore {
  constraints: Array<ValidatorFormat>;
}

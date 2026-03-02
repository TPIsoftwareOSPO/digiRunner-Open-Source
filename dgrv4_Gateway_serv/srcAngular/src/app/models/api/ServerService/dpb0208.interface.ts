import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0208 extends BaseReq {
  ReqBody: DPB0208Req;
}

export interface DPB0208Req {
  clientId: string;
  apiKeyAlias: string;
  effectiveAt?: string;
  expiredAt: string;
  groupIdList: Array<string>;
}

export interface RespDPB0208 extends BaseRes {
  RespBody: DPB0208Resp;
}

export interface DPB0208Resp {
  apiKeyAlias:string;
 }

export interface RespDPB0208Before extends BaseRes {
  RespBody: RespDPB0208RespBefore;
}

export interface RespDPB0208RespBefore {
  constraints: Array<ValidatorFormat>;
}

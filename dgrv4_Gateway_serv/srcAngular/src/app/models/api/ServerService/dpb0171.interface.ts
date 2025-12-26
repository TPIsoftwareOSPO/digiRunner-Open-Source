import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0171 extends BaseReq {
  ReqBody: DPB0171Req;
}
export interface DPB0171Req {
  clientId: string;
  idpType: string;
  status: string;
  remark?: string;
  idpClientId: string;
  idpClientMima: string;
  idpClientName: string;
  wellKnownUrl: string;
  callbackUrl: string;
  authUrl?: string;
  accessTokenUrl?: string;
  scope?: string;
}

export interface RespDPB0171 extends BaseRes { }

export interface RespDPB0171Before extends BaseRes {
  RespBody: RespDPB0171Before;
}

export interface RespDPB0171Before {
  constraints: Array<ValidatorFormat>;
}

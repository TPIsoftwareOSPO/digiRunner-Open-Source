import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0172 extends BaseReq {
  ReqBody: DPB0172Req;
}
export interface DPB0172Req {
  id: string;
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

export interface RespDPB0172 extends BaseRes { }

export interface RespDPB0172Before extends BaseRes {
  RespBody: RespDPB0172Before;
}

export interface RespDPB0172Before {
  constraints: Array<ValidatorFormat>;
}

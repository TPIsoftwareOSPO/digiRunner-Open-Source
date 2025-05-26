import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0170 extends BaseReq {
  ReqBody: DPB0170Req;
}
export interface DPB0170Req {
  id: string;
}

export interface RespDPB0170 extends BaseRes {
  RespBody: DPB0170Resp;
}

export interface DPB0170Resp {
  id: string;
  longId: string;
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
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}




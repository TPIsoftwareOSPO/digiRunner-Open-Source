import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0242 extends BaseReq {
  ReqBody: DPB0242Req;
}

export interface DPB0242Req {
  clientId: string;
  status: string;
  cusLoginUrl: string;
  cusUserDataUrl: string;
  iconFile?: string;
  pageTitle?: string;
}

export interface RespDPB0242 extends BaseRes {
  RespBody: DPB0242Resp;
}

export interface DPB0242Resp {
  gtwIdpInfoCusId: string;
  clientId: string;
  status: string;
  cusLoginUrl: string;
  cusUserDataUrl: string;
  iconFile?: string;
  pageTitle?: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}

export interface RespDPB0242Before extends BaseRes {
  RespBody: RespDPB0242RespBefore;
}

export interface RespDPB0242RespBefore {
  constraints: Array<ValidatorFormat>;
}

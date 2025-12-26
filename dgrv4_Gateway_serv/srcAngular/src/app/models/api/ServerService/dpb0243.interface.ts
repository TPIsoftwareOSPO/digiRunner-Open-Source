import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0243 extends BaseReq {
  ReqBody: DPB0243Req;
}

export interface DPB0243Req {
  gtwIdpInfoCusId: string;
  clientId: string;
  status: string;
  cusLoginUrl: string;
  cusUserDataUrl: string;
  iconFile?: string;
  pageTitle?: string;
}

export interface RespDPB0243 extends BaseRes {
  RespBody: DPB0243Resp;
}

export interface DPB0243Resp {
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

export interface RespDPB0243Before extends BaseRes {
  RespBody: RespDPB0243RespBefore;
}

export interface RespDPB0243RespBefore {
  constraints: Array<ValidatorFormat>;
}

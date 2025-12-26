import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0241 extends BaseReq {
  ReqBody: DPB0241Req;
}

export interface DPB0241Req {
  gtwIdpInfoCusId: string;
  clientId: string;
}

export interface RespDPB0241 extends BaseRes {
  RespBody: DPB0241Resp;
}

export interface DPB0241Resp {
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

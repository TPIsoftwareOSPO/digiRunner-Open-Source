import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0223 extends BaseReq {
  ReqBody: DPB0223Req;
}

export interface DPB0223Req {
  cusId: string;
  cusName?: string;
  cusStatus: string;
  cusLoginUrl: string;
  cusBackendLoginUrl: string;
  cusUserDataUrl: string;
}

export interface RespDPB0223 extends BaseRes {
  RespBody: DPB0223Resp;
}

export interface DPB0223Resp {
  cusId: string;
  cusName?: string;
  cusStatus: string;
  cusLoginUrl: string;
  cusBackendLoginUrl: string;
  cusUserDataUrl: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}

export interface DPB0223RespBefore extends BaseRes {
  RespBody: RespDPB0223RespBefore;
}

export interface RespDPB0223RespBefore {
  constraints: Array<ValidatorFormat>;
}


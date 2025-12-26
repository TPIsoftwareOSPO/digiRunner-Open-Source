import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0222 extends BaseReq {
  ReqBody: DPB0222Req;
}

export interface DPB0222Req {
  cusName?: string;
  cusStatus: string;
  cusLoginUrl: string;
  cusBackendLoginUrl: string;
  cusUserDataUrl: string;
}

export interface RespDPB0222 extends BaseRes {
  RespBody: DPB0222Resp;
}

export interface DPB0222Resp {
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

export interface DPB0222RespBefore extends BaseRes {
  RespBody: RespDPB0222RespBefore;
}

export interface RespDPB0222RespBefore {
  constraints: Array<ValidatorFormat>;
}


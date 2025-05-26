import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0221 extends BaseReq {
  ReqBody: DPB0221Req;
}

export interface DPB0221Req {
  cusId: string;
}

export interface RespDPB0221 extends BaseRes {
  RespBody: DPB0221Resp;
}

export interface DPB0221Resp {
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

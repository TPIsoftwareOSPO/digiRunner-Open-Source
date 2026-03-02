import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0186 extends BaseReq {
  ReqBody: DPB0186Req;
}

export interface DPB0186Req {
  clientId: string;
  status: string;
  remark?: string;
  apiMethod: string;
  apiUrl: string;
  reqHeader?: string;
  reqBodyType: string;
  reqBody?: string;
  sucByType: string;
  sucByField?: string;
  sucByValue?: string;
  idtName?: string;
  idtEmail?: string;
  idtPicture?: string;
  iconFile?: string;
  pageTitle: string;
  idtLightId?: string;
  idtRoleName?: string;
}

export interface RespDPB0186 extends BaseRes {
  RespBody: DPB0186Resp;
}

export interface DPB0186Resp {}

export interface RespDPB0186Before extends BaseRes {
  RespBody: RespDPB0186RespBefore;
}

export interface RespDPB0186RespBefore {
  constraints: Array<ValidatorFormat>;
}

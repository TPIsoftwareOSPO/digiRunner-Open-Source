import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0187 extends BaseReq {
  ReqBody: DPB0187Req;
}

export interface DPB0187Req {
  id: string;
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

export interface RespDPB0187 extends BaseRes {
  RespBody: DPB0187Resp;
}

export interface DPB0187Resp { }

export interface RespDPB0187Before extends BaseRes {
  RespBody: RespDPB0187RespBefore;
}

export interface RespDPB0187RespBefore {
  constraints: Array<ValidatorFormat>;
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0185 extends BaseReq {
  ReqBody: DPB0185Req;
}

export interface DPB0185Req {
  id: string;
}

export interface RespDPB0185 extends BaseRes {
  RespBody: DPB0185Resp;
}

export interface DPB0185Resp {
  id: string;
  longId: string;
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
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
  idtLightId?: string;
  idtRoleName?: string;
}

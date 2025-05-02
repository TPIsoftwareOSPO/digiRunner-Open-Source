import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0196 extends BaseReq {
  ReqBody: DPB0196Req;
}

export interface DPB0196Req {
  id: string;
}

export interface RespDPB0196 extends BaseRes {
  RespBody: DPB0196Resp;
}

export interface DPB0196Resp {
  id: string;
  longId: string;
  clientId: string;
  status: string;
  approvalResultMail: string;
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
}


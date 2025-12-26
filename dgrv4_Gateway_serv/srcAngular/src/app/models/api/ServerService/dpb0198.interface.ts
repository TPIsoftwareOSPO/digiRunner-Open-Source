import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0198 extends BaseReq {
  ReqBody: DPB0198Req;
}

export interface DPB0198Req {
  id: string;
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
}

export interface RespDPB0198 extends BaseRes {
  RespBody: DPB0198Resp;
}

export interface DPB0198Resp { }

export interface RespDPB0198Before extends BaseRes {
  RespBody: RespDPB0198RespBefore;
}

export interface RespDPB0198RespBefore {
  constraints: Array<ValidatorFormat>;
}

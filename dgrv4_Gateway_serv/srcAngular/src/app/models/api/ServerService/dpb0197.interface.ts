import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0197 extends BaseReq {
  ReqBody: DPB0197Req;
}

export interface DPB0197Req {
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

export interface RespDPB0197 extends BaseRes {
  RespBody: DPB0197Resp;
}

export interface DPB0197Resp { }

export interface RespDPB0197Before extends BaseRes {
  RespBody: RespDPB0197RespBefore;
}

export interface RespDPB0197RespBefore {
  constraints: Array<ValidatorFormat>;
}

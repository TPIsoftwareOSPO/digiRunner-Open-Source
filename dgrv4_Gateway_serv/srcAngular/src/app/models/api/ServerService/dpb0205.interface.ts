import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0205 extends BaseReq {
  ReqBody: DPB0205Req;
}

export interface DPB0205Req {
  id: string;
  clientId: string;
  status: string;
  remark?: string;
  connectionName: string;
  sqlPtmt: string;
  sqlParams: string;
  userMimaAlg: string;
  userMimaColName: string;
  idtSub: string;
  idtName?: string;
  idtEmail?: string;
  idtPicture?: string;
  iconFile?: string;
  pageTitle: string;
}

export interface RespDPB0205 extends BaseRes {
  RespBody: DPB0205Resp;
}

export interface DPB0205Resp { }

export interface RespDPB0205Before extends BaseRes {
  RespBody: RespDPB0205RespBefore;
}

export interface RespDPB0205RespBefore {
  constraints: Array<ValidatorFormat>;
}

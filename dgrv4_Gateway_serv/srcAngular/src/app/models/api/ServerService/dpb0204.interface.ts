import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0204 extends BaseReq {
  ReqBody: DPB0204Req;
}

export interface DPB0204Req {
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

export interface RespDPB0204 extends BaseRes {
  RespBody: DPB0204Resp;
}

export interface DPB0204Resp { }

export interface RespDPB0204Before extends BaseRes {
  RespBody: RespDPB0204RespBefore;
}

export interface RespDPB0204RespBefore {
  constraints: Array<ValidatorFormat>;
}

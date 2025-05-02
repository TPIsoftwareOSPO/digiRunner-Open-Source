import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0203 extends BaseReq {
  ReqBody: DPB0203Req;
}

export interface DPB0203Req {
  id: string;
}

export interface RespDPB0203 extends BaseRes {
  RespBody: DPB0203Resp;
}

export interface DPB0203Resp {
  id: string;
  longId: string;
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
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}




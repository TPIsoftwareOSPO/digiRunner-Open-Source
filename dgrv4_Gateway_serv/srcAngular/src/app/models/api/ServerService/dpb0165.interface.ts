import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0165 extends BaseReq {
  ReqBody: DPB0165Req;
}

export interface DPB0165Req {
  id: string;
}

export interface RespDPB0165 extends BaseRes {
  RespBody: DPB0165Resp;
}

export interface DPB0165Resp {
  id: string;
  longId: string;
  clientId: string;
  status: string;
  remark?: string;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  ldapTimeout: string;
  iconFile?: string;
  pageTitle: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}



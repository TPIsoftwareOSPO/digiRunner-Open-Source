import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0164 extends BaseReq {
  ReqBody: DPB0164Req;
}

export interface DPB0164Req {
  clientId: string;
}

export interface RespDPB0164 extends BaseRes {
  RespBody: DPB0164Resp;
}

export interface DPB0164Resp {
  dataList: Array<DPB0164RespItem>;
 }

 export interface DPB0164RespItem {
  id: string;
  longId: string;
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


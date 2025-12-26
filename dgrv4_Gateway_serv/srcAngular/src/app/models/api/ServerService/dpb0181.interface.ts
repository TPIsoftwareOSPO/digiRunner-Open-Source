import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0181 extends BaseReq {
  ReqBody: DPB0181Req;
}

export interface DPB0181Req {
  status: String;
  ldapTimeout: number;
  policy: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
  ldapDataList: Array<DPB0181LdapDataItem>;
}

export interface DPB0181LdapDataItem {
  orderNo: number;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
}

export interface RespDPB0181 extends BaseRes {
  RespBody: DPB0181Resp;
}

export interface DPB0181Resp { }

export interface RespDPB0181Before extends BaseRes {
  RespBody: RespDPB0181RespBefore;
}

export interface RespDPB0181RespBefore {
  constraints: Array<ValidatorFormat>;
}

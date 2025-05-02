import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0182 extends BaseReq {
  ReqBody: DPB0182Req;
}

export interface DPB0182Req {
  masterId: string;
  status: String;
  ldapTimeout: number;
  policy: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
  ldapDataList: Array<DPB0182LdapDataItem>;
}

export interface DPB0182LdapDataItem {
  detailId?: string;
  orderNo: number;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
}

export interface RespDPB0182 extends BaseRes {
  RespBody: DPB0182Resp;
}

export interface DPB0182Resp { }

export interface RespDPB0182Before extends BaseRes {
  RespBody: RespDPB0182RespBefore;
}

export interface RespDPB0182RespBefore {
  constraints: Array<ValidatorFormat>;
}

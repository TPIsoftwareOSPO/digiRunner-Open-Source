import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0180 extends BaseReq {
  ReqBody: DPB0180Req;
}

export interface DPB0180Req {
  masterId: string;
}



export interface RespDPB0180 extends BaseRes {
  RespBody: DPB0180Resp;
}

export interface DPB0180Resp {
  masterId: string;
  masterLongId: string;
  status: String;
  ldapTimeout: number;
  policy: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
  ldapDataList: Array<DPB0180LdapDataItem>;
}

export interface DPB0180LdapDataItem {
  detailId: string;
  detailLongId: string;
  orderNo: number;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}

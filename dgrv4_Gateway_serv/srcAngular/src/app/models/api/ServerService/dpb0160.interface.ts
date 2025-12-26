import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0160 extends BaseReq {
  ReqBody: DPB0160Req;
}
export interface DPB0160Req {
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  ldapTimeout: number;
  ldapStatus: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
}

export interface RespDPB0160 extends BaseRes {

}

export interface RespDPB0160Before extends BaseRes {
  RespBody: DPB0160RespBefore;
}
export interface DPB0160RespBefore {
  constraints: Array<ValidatorFormat>;
}


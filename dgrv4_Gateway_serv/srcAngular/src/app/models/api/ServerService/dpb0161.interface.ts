import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0161 extends BaseReq {
  ReqBody: DPB0161Req;
}
export interface DPB0161Req {
  id: string;
  ldapUrl: string;
  ldapDn: string;
  ldapTimeout: number;
  ldapStatus: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
}

export interface RespDPB0161 extends BaseRes {

}

export interface RespDPB0161Before extends BaseRes {
  RespBody: DPB0161RespBefore;
}
export interface DPB0161RespBefore {
  constraints: Array<ValidatorFormat>;
}


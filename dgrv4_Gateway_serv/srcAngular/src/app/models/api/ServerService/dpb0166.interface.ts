import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0166 extends BaseReq {
  ReqBody: DPB0166Req;
}
export interface DPB0166Req {
  clientId: string;
  status: string;
  remark?: string;
  ldapUrl: string;
  ldapBaseDn: string;
  ldapDn: string;
  ldapTimeout: string;
  iconFile?: string;
  pageTitle: string;
}

export interface RespDPB0166 extends BaseRes {

}

export interface RespDPB0166Before extends BaseRes {
  RespBody: DPB0166RespBefore;
}
export interface DPB0166RespBefore {
  constraints: Array<ValidatorFormat>;
}


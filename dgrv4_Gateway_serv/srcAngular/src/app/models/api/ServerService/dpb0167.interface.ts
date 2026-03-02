import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0167 extends BaseReq {
  ReqBody: DPB0167Req;
}
export interface DPB0167Req {
  id: string;
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

export interface RespDPB0167 extends BaseRes {

}

export interface RespDPB0167Before extends BaseRes {
  RespBody: DPB0167RespBefore;
}
export interface DPB0167RespBefore {
  constraints: Array<ValidatorFormat>;
}


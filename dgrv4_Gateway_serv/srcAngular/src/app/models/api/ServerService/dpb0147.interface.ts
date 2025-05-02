import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0147 extends BaseReq {
  ReqBody: DPB0147Req;
}

export interface DPB0147Req {
  longId: string;
  roleIdList: Array<string>;
  newRoleIdList: Array<string>;
  orgId: string;
  newOrgId: string;
  userName: string;
  newUserName: string;
  newUserAlias?: string;
  newStatus: string;
  newUserEmail?: string;
  newIdpType: string;
  idpType: string;
}

export interface RespDPB0147 extends BaseRes {
  RespBody: DPB0147Resp;
}

export interface DPB0147Resp { }

export interface RespDPB0147Before extends BaseRes {
  RespBody: RespDPB0147RespBefore;
}

export interface RespDPB0147RespBefore {
  constraints: Array<ValidatorFormat>;
}

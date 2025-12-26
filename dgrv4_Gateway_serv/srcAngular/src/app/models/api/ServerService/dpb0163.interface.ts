import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0163 extends BaseReq {
  ReqBody: DPB0163Req;
}

export interface DPB0163Req {
  userName: string;
  userAlias: string;
  status: string;
  userEmail?: string;
  idpType: string;
  orgId?: string;
  roleIdList?: Array<string>;
}

export interface RespDPB0163 extends BaseRes {
  RespBody: DPB0163Resp;
}

export interface DPB0163Resp { }

export interface RespDPB0163Before extends BaseRes {
  RespBody: RespDPB0163RespBefore;
}

export interface RespDPB0163RespBefore {
  constraints: Array<ValidatorFormat>;
}

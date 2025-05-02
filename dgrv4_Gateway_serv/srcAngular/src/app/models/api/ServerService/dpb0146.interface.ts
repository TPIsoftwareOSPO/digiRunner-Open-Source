import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0146 extends BaseReq {
  ReqBody: DPB0146Req;
}
export interface DPB0146Req {
  longId: string;
}

export interface RespDPB0146 extends BaseRes {
  RespBody: DPB0146Resp;
}
export interface DPB0146Resp {
  longId: string;
  orgName: string;
  roleId: Array<string>;
  roleAlias: Array<string>;
  orgId: string;
  userName: string;
  userAlias: string;
  status: string;
  statusName: string;
  userEmail: string;
  idpType: string;
}

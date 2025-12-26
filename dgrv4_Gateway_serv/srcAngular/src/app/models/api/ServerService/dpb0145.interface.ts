import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0145 extends BaseReq {
  ReqBody: DPB0145Req;
}
export interface DPB0145Req { }

export interface RespDPB0145 extends BaseRes {
  RespBody: DPB0145Resp;
}
export interface DPB0145Resp {
  dataList: Array<DPB0145RespItem>
}

export interface DPB0145RespItem {
  id: string;
  longId: string;
  userName: string;
  userAlias: string;
  status: string;
  icon: string;
  idpType: string;
  statusName: string;
  orgId: string;
  orgName: string;
  roleId: Array<string>;
  roleAlias: Array<string>;
}

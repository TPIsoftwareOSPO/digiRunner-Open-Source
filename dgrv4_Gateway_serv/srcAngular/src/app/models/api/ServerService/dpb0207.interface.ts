import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0207 extends BaseReq {
  ReqBody: DPB0207Req;
}

export interface DPB0207Req {
  clientId: string;
}

export interface RespDPB0207 extends BaseRes {
  RespBody: DPB0207Resp;
}

export interface DPB0207Resp {
  dataList:Array<DPB0207RespItem>;
}

export interface DPB0207RespItem {
  id: string;
  longId: string;
  clientId: string;
  apiKeyAlias: string;
  apiKeyMask: string;
  effectiveAt: string;
  expiredAt: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
  groupDataList: Array<DPB0207GroupItem>;
}

export interface DPB0207GroupItem	{
  groupId: string;
  groupName: string;
  groupAlias?: string;
  groupDesc?: string;
}





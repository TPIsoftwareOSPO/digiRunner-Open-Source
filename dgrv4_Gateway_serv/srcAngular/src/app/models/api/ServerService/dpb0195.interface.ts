import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0195 extends BaseReq {
  ReqBody: DPB0195Req;
}

export interface DPB0195Req { }

export interface RespDPB0195 extends BaseRes {
  RespBody: DPB0195Resp;
}

export interface DPB0195Resp {
  dataList: Array<DPB0195RespItem>;
}

export interface DPB0195RespItem {
  id: string;
  longId: string;
  status: string;
  apiMethod: string;
  apiUrl: string;
  approvalResultMail: string;
  iconFile?: string;
  pageTitle: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}


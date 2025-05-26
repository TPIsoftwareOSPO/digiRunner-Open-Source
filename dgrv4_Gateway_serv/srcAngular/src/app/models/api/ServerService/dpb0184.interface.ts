import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0184 extends BaseReq {
  ReqBody: DPB0184Req;
}

export interface DPB0184Req {
  clientId: string;
}

export interface RespDPB0184 extends BaseRes {
  RespBody: DPB0184Resp;
}

export interface DPB0184Resp {
  dataList: Array<DPB0184RespItem>;
}

export interface DPB0184RespItem {
  id: string;
  longId: string;
  clientId: string;
  status: string;
  apiMethod: string;
  apiUrl: string;
  remark?: string;
  iconFile?: string;
  pageTitle: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}


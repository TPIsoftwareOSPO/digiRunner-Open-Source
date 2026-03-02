import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0202 extends BaseReq {
  ReqBody: DPB0202Req;
}

export interface DPB0202Req {
  clientId: string;
}

export interface RespDPB0202 extends BaseRes {
  RespBody: DPB0202Resp;
}

export interface DPB0202Resp {
  dataList: Array<DPB0202RespItem>;
}

export interface DPB0202RespItem {
  id: string;
  longId: string;
  clientId: string;
  status: string;
  remark?: string;
  connectionName: string;
  sqlPtmt: string;
  iconFile?: string;
  pageTitle: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}


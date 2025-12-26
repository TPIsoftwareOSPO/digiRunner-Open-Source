import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0232 extends BaseReq {
  ReqBody: DPB0232Req;
}

export interface DPB0232Req {}

export interface RespDPB0232 extends BaseRes {
  RespBody: DPB0232Resp;
}

export interface DPB0232Resp {
  status: string;
  dataList: Array<DPB0232WhitelistItem>;
}

export interface DPB0232WhitelistItem {
  id: string;
  longId: string;
  rule: string;
  type: string;
  createDateTime: string;
  createUser: string;
  updateDateTime?: string;
  updateUser?: string;
}

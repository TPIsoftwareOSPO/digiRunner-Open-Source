import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0233 extends BaseReq {
  ReqBody: DPB0233Req;
}

export interface DPB0233Req {
  status: string;
  whitelistDataList?: Array<DPB0233WhitelistItem>|null;
}

export interface DPB0233WhitelistItem {
  id?: string;
  rule: string;
}

export interface RespDPB0233 extends BaseRes {
  RespBody: DPB0233Resp;
}

export interface DPB0233Resp {}


export interface DPB0233RespBefore extends BaseRes {
  RespBody: RespDPB0233RespBefore;
}

export interface RespDPB0233RespBefore {
  constraints: Array<ValidatorFormat>;
}

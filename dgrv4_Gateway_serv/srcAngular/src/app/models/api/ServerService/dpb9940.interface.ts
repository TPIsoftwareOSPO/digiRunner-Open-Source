import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9940 extends BaseReq {
  ReqBody: DPB9940Req;
}

export interface DPB9940Req {
  exportItemList: string[];
  exportLocaleList: string[];
}

export interface RespDPB9940 extends BaseRes {
  RespBody: DPB9940Resp;
}

export interface DPB9940Resp {}

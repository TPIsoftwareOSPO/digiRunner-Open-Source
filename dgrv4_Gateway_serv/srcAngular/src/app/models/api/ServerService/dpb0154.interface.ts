import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0154 extends BaseReq {
  ReqBody: DPB0154Req;
}

export interface DPB0154Req {
  websiteStatus: string;
  websiteName: string;
  webSiteList: Array<DPB0154ItemReq>;
  remark?: string;
  auth: string;
  sqlInjection: string;
  traffic: string;
  xss: string;
  xxe: string;
  tps: string;
  ignoreApi?: string;
  showLog: string;
}

export interface DPB0154ItemReq {
  probability: string;
  url: string;
}

export interface RespDPB0154 extends BaseRes {
  RespBody: DPB0154Resp;
}

export interface DPB0154Resp { }


export interface RespDPB0154Before extends BaseRes {
  RespBody: DPB0154RespBefore;
}

export interface DPB0154RespBefore {
  constraints: Array<ValidatorFormat>;
}


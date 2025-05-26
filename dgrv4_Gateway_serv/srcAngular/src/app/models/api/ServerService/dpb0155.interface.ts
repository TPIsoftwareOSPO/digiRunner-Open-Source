import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0155 extends BaseReq {
    ReqBody: DPB0155Req;
}

export interface DPB0155Req {
  dgrWebsiteId: string;
  websiteStatus: string;
  websiteName: string;
  webSiteList: Array<DPB0155ItemReq>;
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

export interface DPB0155ItemReq {
  probability: string;
  url: string;
}

export interface RespDPB0155 extends BaseRes {
    RespBody: DPB0155Resp;
}

export interface DPB0155Resp { }


export interface RespDPB0155Before extends BaseRes {
    RespBody: DPB0155RespBefore;
}

export interface DPB0155RespBefore {
    constraints: Array<ValidatorFormat>;
}


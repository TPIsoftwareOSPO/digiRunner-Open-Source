import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0158 extends BaseReq {
  ReqBody: DPB0158Req;
}
export interface DPB0158Req {
  id: string;
}

export interface RespDPB0158 extends BaseRes {
  RespBody: DPB0158Resp;
}
export interface DPB0158Resp {
  websiteStatus: string;
  websiteName: string;
  webSiteList: Array<DPB0158ItemReq>
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

export interface DPB0158ItemReq {
  probability: string;
  url: string;
}

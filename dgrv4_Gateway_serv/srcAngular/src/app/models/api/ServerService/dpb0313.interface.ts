import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0313 extends BaseReq {
  ReqBody: DPB0313Req;
}

export interface DPB0313Req {
  proxyList: Array<DPB0313ReqItem>;
}

export interface DPB0313ReqItem {
  sofProxyId: string;
  version: string;
}

export interface RespDPB0313 extends BaseRes {
  RespBody: DPB0313Resp;
}

export interface DPB0313Resp {
  successCount: number;
  deletedProxyIds: Array<string>;
}

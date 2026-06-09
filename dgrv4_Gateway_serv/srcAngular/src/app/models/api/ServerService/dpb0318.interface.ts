import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0318 extends BaseReq {
  ReqBody: DPB0318Req;
}

export interface DPB0318Req {
  sofProxyId: string;
  sofProxyDiversionId: string;
  sofProxyStickyType: string;
  sofProxyStickyTypeId?: string;
  sofProxyStickyVerb?: string;
  sofProxyStickyPath?: string;
  sofProxyStickyInteraction?: string;
}

export interface RespDPB0318 extends BaseRes {
  RespBody: DPB0318Resp;
}

export interface DPB0318Resp {
  sofProxyStickyId: string;
  sofProxyId: string;
  sofProxyDiversionId: string;
  sofProxyStickyInteraction: string;
  sofProxyStickyType: string;
  sofProxyStickyTypeId: string;
  sofProxyStickyVerb: string;
  sofProxyStickyPath: string;
  sofProxyStickyHashcode: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
  version: string;
}

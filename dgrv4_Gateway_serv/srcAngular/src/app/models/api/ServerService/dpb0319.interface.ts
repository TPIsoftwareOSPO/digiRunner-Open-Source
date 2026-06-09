import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0319 extends BaseReq {
  ReqBody: DPB0319Req;
}

export interface DPB0319Req {
  stickyList: SmartOnFhirProxyStickyUpdateDto[];
}

export interface SmartOnFhirProxyStickyUpdateDto {
  sofProxyStickyId: string;
  version: string;
  sofProxyDiversionId?: string;
  sofProxyStickyType?: string;
  sofProxyStickyTypeId?: string;
  sofProxyStickyVerb?: string;
  sofProxyStickyPath?: string;
  sofProxyStickyInteraction?: string;
}

export interface RespDPB0319 extends BaseRes {
  RespBody: DPB0319Resp;
}

export interface DPB0319Resp {
  successCount: number;
  updatedStickies: SmartOnFhirProxyStickyDto[];
}

export interface SmartOnFhirProxyStickyDto {
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

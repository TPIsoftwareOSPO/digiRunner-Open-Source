import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0317 extends BaseReq {
  ReqBody: SmartOnFhirProxyStickySearchReq;
}

export interface SmartOnFhirProxyStickySearchReq {
  pageNum?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: string;
  sofProxyId?: string;
  sofProxyDiversionId?: string;
  sofProxyStickyType?: string;
  keywords?: string[];
}

export interface RespDPB0317 extends BaseRes {
  RespBody: DPB0317Resp;
}

export interface DPB0317Resp {
  content: SmartOnFhirProxyStickyDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
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

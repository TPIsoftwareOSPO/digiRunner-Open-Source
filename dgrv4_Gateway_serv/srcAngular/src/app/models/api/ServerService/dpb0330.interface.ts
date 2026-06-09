import { BaseReq, BaseRes } from '../base.interface';

// ==================== DPB0330：查詢 SMART Client 列表（分頁） ====================

export interface ReqDPB0330 extends BaseReq {
  ReqBody: DPB0330ReqBody;
}

export interface DPB0330ReqBody {
  pageNum?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: string;
  keyword?: string;
}

export interface RespDPB0330 extends BaseRes {
  RespBody: DPB0330RespBody;
}

/** 泛型分頁回應，對應後端 PageResp<SmartClientDto> */
export interface DPB0330RespBody {
  content: SmartClientDto[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

/** SMART Client 查詢回應 DTO，對應後端 SmartClientDto */
export interface SmartClientDto {
  smartClientId?: string;
  clientId?: string;
  clientType?: string;
  idpType?: string;
  idpClientId?: string;
  tokenEndpointAuthMethod?: string;
  allowedScopes?: string;
  redirectUris?: string;
  launchMode?: string;
  autoApprove?: string;
  jwksUri?: string;
  /** 列表查詢不回傳 JWKS 原文，只回傳是否有值 */
  hasJwks?: boolean;
  createDateTime?: string | number;
  createUser?: string;
  updateDateTime?: string | number;
  updateUser?: string;
  version?: string;
}

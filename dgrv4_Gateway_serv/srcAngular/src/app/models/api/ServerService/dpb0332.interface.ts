import { BaseReq, BaseRes } from '../base.interface';
import { SmartClientDto } from './dpb0330.interface';

// ==================== DPB0332：批次更新 SMART Client（PATCH 語意） ====================

export interface ReqDPB0332 extends BaseReq {
  ReqBody: DPB0332ReqBody;
}

export interface DPB0332ReqBody {
  updateList: SmartClientUpdateItem[];
}

/**
 * 批次更新的單筆項目。
 *
 * PATCH 語意：欄位不存在 = 不更新、null = 清除、有值 = 更新。
 * 後端使用 JsonNullable 區分三態，前端只需要控制 JSON 的欄位是否出現。
 */
export interface SmartClientUpdateItem {
  /** 識別更新目標（必填） */
  clientId: string;
  /** 樂觀鎖版本號（必填） */
  version: number;
  clientType?: string | null;
  idpType?: string | null;
  allowedScopes?: string[] | null;
  redirectUris?: string[] | null;
  launchMode?: string | null;
  autoApprove?: string | null;
  tokenEndpointAuthMethod?: string | null;
  jwksUri?: string | null;
  jwks?: string | null;
}

export interface RespDPB0332 extends BaseRes {
  RespBody: SmartClientDto[];
}

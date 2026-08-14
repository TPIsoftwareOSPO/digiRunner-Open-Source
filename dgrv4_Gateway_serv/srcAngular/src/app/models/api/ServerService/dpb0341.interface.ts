import { BaseReq, BaseRes } from '../base.interface';
import { SmartOnFhirProxyDto } from './dpb0311.interface';

// DPB0341: 測試後端代取 token（不寫 DB），req 沿用 DPB0311 DTO，僅帶 out_* 代取設定
export interface ReqDPB0341 extends BaseReq {
  ReqBody: Partial<SmartOnFhirProxyDto>;
}

export interface RespDPB0341 extends BaseRes {
  RespBody: DPB0341Resp;
}

export interface DPB0341Resp {
  success: boolean; // 代取是否成功
  message: string; // 成功 "OK"，失敗則為原因
  tokenEndpoint: string; // 實際使用的 token endpoint（discovery 解析後）
  tokenObtained: boolean; // 是否取得 token
  token: string; // 代取取得的完整 token（診斷用）
  expiresInSec: number; // token 有效秒數
}

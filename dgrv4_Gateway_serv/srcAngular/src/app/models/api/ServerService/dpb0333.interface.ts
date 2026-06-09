import { BaseReq, BaseRes } from '../base.interface';

// ==================== DPB0333：批次刪除 SMART Client ====================

export interface ReqDPB0333 extends BaseReq {
  ReqBody: DPB0333ReqBody;
}

export interface DPB0333ReqBody {
  deleteList: SmartClientDeleteItem[];
}

export interface SmartClientDeleteItem {
  /** 識別刪除目標（必填） */
  clientId: string;
  /** 樂觀鎖版本號（必填） */
  version: number;
}

export interface RespDPB0333 extends BaseRes {
  RespBody: DPB0333RespBody;
}

export interface DPB0333RespBody {
  deletedCount: number;
}

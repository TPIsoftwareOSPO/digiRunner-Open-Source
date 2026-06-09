import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0315 extends BaseReq {
  ReqBody: DPB0315Req;
}

export interface DPB0315Req {
  file?: any;
  jsonData?: string;
}

export interface RespDPB0315 extends BaseRes {
  RespBody:  DPB0315Resp;
}

export interface DPB0315Resp {
  type: string;
  current?: number;
  total?: number;
  percentage?: number;
  status?: string;
  message?: string;
  data?: any;
  successCount?: number;
  failureCount?: number;
  totalCount?: number;
  summary?: string;
  fileName?: string;
}

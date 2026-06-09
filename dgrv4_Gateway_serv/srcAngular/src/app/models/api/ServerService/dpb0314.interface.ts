import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0314 extends BaseReq {
  ReqBody: SmartOnFhirProxySearchReq;
}

export interface SmartOnFhirProxySearchReq {
  pageNum?: number;
  pageSize?: number;
  sortBy?: string;
  sortOrder?: string;
  keywords?: Array<string>;
  sofProxyStatus?: string;
}

export interface RespDPB0314 extends BaseRes {
  RespBody: ImportExportProgressEvent;
}

export interface ImportExportProgressEvent {
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


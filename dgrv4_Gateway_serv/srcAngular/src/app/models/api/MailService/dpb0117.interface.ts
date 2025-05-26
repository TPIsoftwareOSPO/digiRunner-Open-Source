import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0117 extends BaseReq {
  ReqBody: DPB0117Req;
}
export interface DPB0117Req {
  id: number;
  recipients: string;
}

export interface ResDPB0117 extends BaseRes {
  RespBody: DPB0117Resp;
}
export interface DPB0117Resp {
  mailLogId: number;
  recipients: string;
  subject: string; // 主旨
  content: string; // 內容
  result: string; // 0: 失敗、1: 成功
  refCode: string;
  createDate: string;
  createUser: string;
  errorMsg?: string;
}

import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0285 extends BaseReq {
  ReqBody: DPB0285Req;
}

export interface DPB0285Req {
  webhookNotifyLogId?:string;
  startDate?:string;
  endDate?:string;
  keyword?:string;
  paging?:string;
}

export interface RespDPB0285 extends BaseRes {
  RespBody: DPB0285Resp;
}

export interface DPB0285Resp {
  webhookLogList: Array<DPB0285WebhookLogItem>;
}

export interface DPB0285WebhookLogItem {
  webhookNotifyLogId: string;
  notifyName: string;
  notifyType: string;
  clientId: string;
  content: string;
  remark: string;
  createDateTime: string;
  result:string;
}

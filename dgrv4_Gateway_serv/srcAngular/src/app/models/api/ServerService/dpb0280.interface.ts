import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0280 extends BaseReq {
  ReqBody: DPB0280Req;
}

export interface DPB0280Req {
  webhookNotifyId?: string;
  keyword?: string;
  enable?: string;
  paging?: string;
}

export interface RespDPB0280 extends BaseRes {
  RespBody: DPB0280Resp;
}

export interface DPB0280Resp {
  webhookNotifyList: Array<DPB0280WebhookNotify>;
}

export interface DPB0280WebhookNotify {
  webhookNotifyId: string;
  notifyName: string;
  notifyType: string;
  enable: string;
  createDateTime: string;
  createUser: string;
}

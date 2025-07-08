import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0282 extends BaseReq {
  ReqBody: DPB0282Req;
}

export interface DPB0282Req {
  webhookNotifyId: string;
}

export interface RespDPB0282 extends BaseRes {
  RespBody: DPB0282Resp;
}

export interface DPB0282Resp {
  webhookNotifyId: string;
  notifyName: string;
  notifyType: string;
  enable: string;
  message?: string;
  payloadFlag: string;
  fieldList: Array<DPB0282Field>
}

export interface DPB0282Field {
  key: string;
  value: string;
  type: string;
  mappingUrl?: string;
}

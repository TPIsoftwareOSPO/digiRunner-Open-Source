import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0283 extends BaseReq {
  ReqBody: DPB0283Req;
}

export interface DPB0283Req {
  webhookNotifyId:string;
  enable: string;
  message?: string;
  payloadFlag: boolean;
  fieldList?: Array<DPB0283Field>;
}

export interface DPB0283Field {
  key: string;
  value: string;
}

export interface RespDPB0283 extends BaseRes {
  RespBody: DPB0283Resp;
}

export interface DPB0283Resp {
  webhookNotifyId: string;
}

export interface RespDPB0283Before extends BaseRes {
    RespBody: DPB0283RespBefore;
}

export interface DPB0283RespBefore {
    constraints: Array<ValidatorFormat>;
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0281 extends BaseReq {
  ReqBody: DPB0281Req;
}

export interface DPB0281Req {
  notifyName: string;
  notifyType: string;
  enable: string;
  message?: string;
  payloadFlag: string;
  fieldList?: Array<DPB0281Field>;
  reqBlockList?: Array<DPB0281ReqBlock>;
}

export interface DPB0281ReqBlock {
  url?:string;
  fieldList: Array<DPB0281Field>;
}

export interface DPB0281Field {
  key: string;
  value: string;
  type: string;
  mappingUrl?:string;
}

export interface RespDPB0281 extends BaseRes {
  RespBody: DPB0281Resp;
}

export interface DPB0281Resp {
  webhookNotifyId: string;
}

export interface RespDPB0281Before extends BaseRes {
  RespBody: DPB0281RespBefore;
}

export interface DPB0281RespBefore {
  constraints: Array<ValidatorFormat>;
}

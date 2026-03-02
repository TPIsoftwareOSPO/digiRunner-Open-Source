import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0194 extends BaseReq {
  ReqBody: DPB0194Req;
}

export interface DPB0194Req {
  connectionName: string;
}

export interface RespDPB0194 extends BaseRes {
  RespBody: DPB0194Resp;
}

export interface DPB0194Resp { }

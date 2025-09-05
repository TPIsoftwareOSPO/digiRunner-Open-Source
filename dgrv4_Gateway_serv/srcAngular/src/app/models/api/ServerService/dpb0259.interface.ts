import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0259 extends BaseReq {
  ReqBody: DPB0259Req;
}

export interface DPB0259Req {}

export interface RespDPB0259 extends BaseRes {
  RespBody: DPB0259Resp;
}

export interface DPB0259Resp {}

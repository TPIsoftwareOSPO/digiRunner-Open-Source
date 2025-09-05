import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0266 extends BaseReq {
  ReqBody: DPB0266Req;
}

export interface DPB0266Req {}

export interface RespDPB0266 extends BaseRes {
  RespBody: DPB0266Resp;
}

export interface DPB0266Resp {}

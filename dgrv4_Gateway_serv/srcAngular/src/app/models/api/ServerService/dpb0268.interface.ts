import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0268 extends BaseReq {
  ReqBody: DPB0268Req;
}

export interface DPB0268Req {}

export interface RespDPB0268 extends BaseRes {
  RespBody: DPB0268Resp;
}

export interface DPB0268Resp {}

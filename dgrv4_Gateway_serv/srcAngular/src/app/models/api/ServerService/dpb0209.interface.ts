import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0209 extends BaseReq {
  ReqBody: DPB0209Req;
}

export interface DPB0209Req {
  id: string;
}

export interface RespDPB0209 extends BaseRes {
  RespBody: DPB0209Resp;
}

export interface DPB0209Resp { }

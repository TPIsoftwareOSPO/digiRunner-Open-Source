import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0188 extends BaseReq {
  ReqBody: DPB0188Req;
}

export interface DPB0188Req {
  id: string;
}

export interface RespDPB0188 extends BaseRes {
  RespBody: DPB0188Resp;
}

export interface DPB0188Resp { }

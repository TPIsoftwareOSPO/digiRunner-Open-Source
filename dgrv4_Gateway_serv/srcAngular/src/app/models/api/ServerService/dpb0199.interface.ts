import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0199 extends BaseReq {
  ReqBody: DPB0199Req;
}

export interface DPB0199Req {
  id: string;
}

export interface RespDPB0199 extends BaseRes {
  RespBody: DPB0199Resp;
}

export interface DPB0199Resp { }

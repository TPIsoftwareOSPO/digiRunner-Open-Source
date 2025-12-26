import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0206 extends BaseReq {
  ReqBody: DPB0206Req;
}

export interface DPB0206Req {
  id: string;
}

export interface RespDPB0206 extends BaseRes {
  RespBody: DPB0206Resp;
}

export interface DPB0206Resp { }

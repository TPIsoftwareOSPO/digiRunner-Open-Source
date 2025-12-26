import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0224 extends BaseReq {
  ReqBody: DPB0224Req;
}

export interface DPB0224Req {
  cusId: string;
}

export interface RespDPB0224 extends BaseRes { }


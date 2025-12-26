import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0168 extends BaseReq {
  ReqBody: DPB0168Req;
}
export interface DPB0168Req {
  id: string;
}

export interface RespDPB0168 extends BaseRes { }


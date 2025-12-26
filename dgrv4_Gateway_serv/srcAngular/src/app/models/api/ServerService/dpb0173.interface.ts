import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0173 extends BaseReq {
  ReqBody: DPB0173Req;
}
export interface DPB0173Req {
  id: string;
}

export interface RespDPB0173 extends BaseRes { }



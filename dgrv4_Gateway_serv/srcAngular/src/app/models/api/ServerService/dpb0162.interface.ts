import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0162 extends BaseReq {
  ReqBody: DPB0162Req;
}
export interface DPB0162Req {
  id: string;
}

export interface RespDPB0162 extends BaseRes { }


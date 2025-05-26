import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0183 extends BaseReq {
  ReqBody: DPB0183Req;
}
export interface DPB0183Req {
  masterId: string;
}

export interface RespDPB0183 extends BaseRes { }


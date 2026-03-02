import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9927 extends BaseReq {
  ReqBody: DPB9927Req;
}

export interface DPB9927Req {
  funcType: string;
}

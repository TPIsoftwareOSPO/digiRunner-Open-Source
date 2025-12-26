import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0148 extends BaseReq {
  ReqBody: DPB0148Req;
}
export interface DPB0148Req {
  longId: string;
}

export interface RespDPB0148 extends BaseRes {
  RespBody: DPB0148Resp;
}
export interface DPB0148Resp {

}

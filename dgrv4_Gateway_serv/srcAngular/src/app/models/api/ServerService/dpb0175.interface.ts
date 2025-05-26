import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0175 extends BaseReq {
  ReqBody: DPB0175Req;
}

export interface DPB0175Req {
  longId: string;
}

export interface RespDPB0175 extends BaseRes {
  RespBody: DPB0175Resp;
}

export interface DPB0175Resp {
  hexId: string;
  longId: string;
  siteName: string;
  targetWs: string;
  memo: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
  auth: string;
 }



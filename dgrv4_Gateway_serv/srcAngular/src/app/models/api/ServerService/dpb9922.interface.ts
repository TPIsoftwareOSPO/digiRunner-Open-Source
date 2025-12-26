import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9922 extends BaseReq {
    ReqBody: DPB9922Req;
}

export interface DPB9922Req {
  file: File,
  req: string
}

export interface RespDPB9922 extends BaseRes {
  RespBody: DPB9922Resp;
}

export interface DPB9922Resp { }


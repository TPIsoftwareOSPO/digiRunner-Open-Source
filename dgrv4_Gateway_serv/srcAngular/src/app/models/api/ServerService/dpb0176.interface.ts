import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0176 extends BaseReq {
  ReqBody: DPB0176Req;
}

export interface DPB0176Req {
  siteName: string;
  targetWs: string;
  memo?: string;
  auth: string;
}

export interface RespDPB0176 extends BaseRes {
  RespBody: DPB0176Resp;
}

export interface DPB0176Resp {

}

 export interface RespDPB0176Before extends BaseRes {
  RespBody: RespDPB0176RespBefore;
}

export interface RespDPB0176RespBefore {
  constraints: Array<ValidatorFormat>;
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0177 extends BaseReq {
  ReqBody: DPB0177Req;
}

export interface DPB0177Req {
  longId: string;
  targetWs: string;
  memo?: string;
}

export interface RespDPB0177 extends BaseRes {
  RespBody: DPB0177Resp;
}

export interface DPB0177Resp {

}

 export interface RespDPB0177Before extends BaseRes {
  RespBody: RespDPB0177RespBefore;
}

export interface RespDPB0177RespBefore {
  constraints: Array<ValidatorFormat>;
}

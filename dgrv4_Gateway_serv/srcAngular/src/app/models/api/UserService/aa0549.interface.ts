import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0549 extends BaseReq {
  ReqBody: AA0549Req;
}
export interface AA0549Req {
  oriMima: string;
  newMima: string;
}

export interface ResAA0549 extends BaseRes {
  RespBody: AA0549Resp;
}
export interface AA0549Resp { }

export interface ResAA0549Before extends BaseRes {
  RespBody: ResAA0549RespBefore;
}
export interface ResAA0549RespBefore {
  constraints: Array<ValidatorFormat>;
}

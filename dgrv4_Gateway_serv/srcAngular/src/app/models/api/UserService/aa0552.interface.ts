import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0552 extends BaseReq {
  ReqBody: AA0552Req;
}
export interface AA0552Req {
  email: string;
  userList: Array<string>;
  expireKey: string;
  newMima: string;
}

export interface ResAA0552 extends BaseRes {
  RespBody: AA0552Resp;
}
export interface AA0552Resp { }

export interface ResAA0552Before extends BaseRes {
  RespBody: ResAA0552RespBefore;
}
export interface ResAA0552RespBefore {
  constraints: Array<ValidatorFormat>;
}

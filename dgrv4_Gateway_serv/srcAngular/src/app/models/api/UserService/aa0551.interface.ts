import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0551 extends BaseReq {
  ReqBody: AA0551Req;
}
export interface AA0551Req {
  email: string;
  otpCode: string;
  expireKey: string;
}

export interface ResAA0551 extends BaseRes {
  RespBody: AA0551Resp;
}
export interface AA0551Resp {
  userList: Array<string>;
}

export interface ResAA0551Before extends BaseRes {
  RespBody: ResAA0551RespBefore;
}
export interface ResAA0551RespBefore {
  constraints: Array<ValidatorFormat>;
}

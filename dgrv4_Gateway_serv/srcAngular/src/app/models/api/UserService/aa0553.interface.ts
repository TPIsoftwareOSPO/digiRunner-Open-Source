import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0553 extends BaseReq {
  ReqBody: QueryString;
}
export interface QueryString {}

export interface ResAA0553 extends BaseRes {
  RespBody: AA0553Resp;
}
export interface AA0553Resp {
  acPwdStrength: string;
  acPwdStrengthDesc: string;
  clientPwdStrength: string;
  clientPwdStrengthDesc: string;
}

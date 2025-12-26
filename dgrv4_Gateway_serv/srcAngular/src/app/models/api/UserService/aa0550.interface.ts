import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0550 extends BaseReq {
  ReqBody: AA0550Req;
}
export interface AA0550Req {
  email: string;
}

export interface ResAA0550 extends BaseRes {
  RespBody: AA0550Resp;
}
export interface AA0550Resp {
  expireKey:string;
}

export interface ResAA0550Before extends BaseRes {
  RespBody: ResAA0550RespBefore;
}
export interface ResAA0550RespBefore {
  constraints: Array<ValidatorFormat>;
}

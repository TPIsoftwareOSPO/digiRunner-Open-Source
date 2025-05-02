import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0107 extends BaseReq {
  ReqBody: AA0107Req;
}

export interface AA0107Req {
  funcCode: string;
  locale: string;
}

export interface ResAA0107 extends BaseRes {
  RespBody: AA0107Resp;
}

export interface AA0107Resp {}

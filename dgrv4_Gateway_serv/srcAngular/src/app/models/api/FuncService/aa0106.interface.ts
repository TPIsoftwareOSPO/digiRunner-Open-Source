import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0106 extends BaseReq {
  ReqBody: AA0106Req;
}
/**
 * type=1時isRequired, maxLengh(2000) 改由service檢查
 * type=1且有host的情況為必填
 */
export interface AA0106Req {
  embeddedUrl?: string;
  funcList: Array<AA0106ReqItem>;
  type: string; //0:主選單, 1:子選單
  masterFuncCode?: string;
  isKibana?: string;
}

export interface AA0106ReqItem {
  locale: string;
  funcName: string;
  funcDesc?: string;
}

export interface ResAA0106 extends BaseRes {
  RespBody: AA0106Resp;
}

export interface AA0106Resp {}

export interface RespAA0106Before extends BaseRes {
  RespBody:AA0106RespBefore;
}

export interface AA0106RespBefore {
  constraints: Array<ValidatorFormat>;
}


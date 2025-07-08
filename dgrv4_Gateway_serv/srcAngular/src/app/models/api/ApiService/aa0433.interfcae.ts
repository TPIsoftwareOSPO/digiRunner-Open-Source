import { BaseReq, BaseRes } from '../base.interface';
import { AA1002List } from '../OrgService/aa1002.interface';

export interface ReqAA0433 extends BaseReq {
  ReqBody: AA0433Req;
}

export interface AA0433Req {
  orgID: String|undefined;
}

export interface RespAA0433 extends BaseRes {
  RespBody: AA0433Resp;
}

export interface AA0433Resp {
  orgList: AA1002List[];
}

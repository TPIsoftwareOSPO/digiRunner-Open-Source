import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0108 extends BaseReq {
  ReqBody: AA0108Req;
}

export interface AA0108Req { }

export interface ResAA0108 extends BaseRes {
  RespBody: AA0108Resp;
}

export interface AA0108Resp {
  funcInfoList: Array<AA0108FuncInfo>;
}

export interface AA0108FuncInfo {
  funcCode: string
  funcName: string
}

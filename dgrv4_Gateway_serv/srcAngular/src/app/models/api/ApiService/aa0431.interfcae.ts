import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0431 extends BaseReq {
  ReqBody: AA0431Req;
}

export interface AA0431Req {
  apiList: Array<AA0431ReqItem>;
  labelList: Array<string>;
}

export interface RespAA0431 extends BaseRes {
  RespBody: AA0431Resp;
}

export interface AA0431Resp { }

export interface AA0431ReqItem {
  apiKey: string;
  moduleName: string;
}

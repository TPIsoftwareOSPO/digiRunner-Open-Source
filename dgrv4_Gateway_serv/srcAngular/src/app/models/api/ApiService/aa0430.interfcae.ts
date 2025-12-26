import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0430 extends BaseReq {
  ReqBody: AA0430Req;
}
export interface AA0430Req {
  apiList: Array<AA0430ReqItem>;
  noOauth: boolean;
}

export interface RespAA0430 extends BaseRes {
  RespBody: AA0430Resp;
}

export interface AA0430Resp {

}
export interface AA0430ReqItem {
  apiKey: string;
  moduleName: string;
}

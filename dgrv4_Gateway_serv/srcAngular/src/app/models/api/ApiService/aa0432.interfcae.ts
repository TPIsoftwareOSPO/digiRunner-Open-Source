import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0432 extends BaseReq {
  ReqBody: AA0432Req;
}

export interface AA0432Req {
  apiList: Array<AA0432ReqItem>;
  failDiscoveryPolicy: string;
  failHandlePolicy: string;
}

export interface RespAA0432 extends BaseRes {
  RespBody: AA0432Resp;
}

export interface AA0432Resp {}

export interface AA0432ReqItem {
  apiKey: string;
  moduleName: string;
}

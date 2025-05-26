import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0426 extends BaseReq {
    ReqBody: AA0426Req;
}
export interface AA0426Req {
  tempFileName:string;
  refId:string;
}

export interface RespAA0426 extends BaseRes {
    RespBody: AA0426Resp;
}

export interface AA0426Resp {
  errList: Array<AA0426RespItem>;
  errMsg: string;
}

export interface AA0426RespItem {
  apiKey: string;
  moduleName: string;
}


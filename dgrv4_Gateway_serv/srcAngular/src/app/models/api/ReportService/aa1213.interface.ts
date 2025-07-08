import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1213 extends BaseReq {
  ReqBody: AA1213Req;
}
export interface AA1213Req {
  queryItem:number;
  abnormalElapsedTime: number;
}

export interface ResAA1213 extends BaseRes {
  RespBody: AA1213Resp;
}
export interface AA1213Resp {
  dataList: Array<AA1213RespItem>;
}
export interface AA1213RespItem {
  nodeName: string;
  apiName: string;
  uri: string;
  labelList?: Array<string>;
  statusCode: number;
  elapsedTime: number;
}

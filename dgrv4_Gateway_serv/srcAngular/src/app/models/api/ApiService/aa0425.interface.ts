import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0425 extends BaseReq {
  ReqBody: AA0425Req;
}
export interface AA0425Req {
  tempFileName: string;
  sort?: number;
  refId: string;
}

export interface RespAA0425 extends BaseRes {
  RespBody: AA0425Resp;
}

export interface AA0425Resp {
  apiList: Array<AA0425RespItem>;
}

export interface AA0425RespItem {
  sort: number;
  apiKey: string;
  moduleName: string;
  apiName: string
  oldSrcUrlList: Array<AA0425RespIpAndSrcUrlList>;
  newSrcUrlList?: Array<AA0425RespIpAndSrcUrlList>;
  apiStatus: string
  noAuth: boolean,
  labelList: Array<string>
}

export interface AA0425RespIpAndSrcUrlList {
  ip: string;
  srcUrlList: Array<AA0425RespPercentageAndSrcUrl>;
}
export interface AA0425RespPercentageAndSrcUrl {
  percentage: number;
  srcUrl: string;
}

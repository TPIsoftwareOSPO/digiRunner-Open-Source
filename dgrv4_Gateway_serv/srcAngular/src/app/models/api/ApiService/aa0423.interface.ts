import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0423 extends BaseReq {
  ReqBody: AA0423Req;
}
export interface AA0423Req {
  labelList?: Array<string>;
  targetSite?: Array<string>;
}

export interface RespAA0423 extends BaseRes {
  RespBody: AA0423Resp;
}

export interface AA0423Resp {
  dataList: Array<AA0423RespItem>;
}

export interface AA0423RespItem {
  apiKey: string;
  moduleName: string;
  apiStatus: string;
  apiName: string;
  srcUrlList: Array<AA0423RespSrcUrlListItem>;
  noOauth: string;
  labelList?: Array<string>
}

export interface AA0423RespSrcUrlListItem {
  ip: string;
  srcUrlAndPercentageList: Array<AA0423RespSrcUrlAndPercentageItem>
}

export interface AA0423RespSrcUrlAndPercentageItem {
  percentage: string;
  srcUrl: string;
}

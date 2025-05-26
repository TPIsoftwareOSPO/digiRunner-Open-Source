import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0424 extends BaseReq {
  ReqBody: AA0424Req;
}
export interface AA0424Req {
  apiList: Array<AA0424ReqAPIList>;
  srcUrlList?: Array<AA0424ReqSrcUrlList>;
}

export interface RespAA0424 extends BaseRes {
  RespBody: AA0424Resp;
}

export interface AA0424Resp {
  tempFileName: string;
  refId: string;
}

export interface AA0424ReqSrcUrlList {
  srcUrl: string;
  isPercentage: boolean;
  percentage?: number;
  isReplace: boolean;
  replaceString?: string;
}

export interface AA0424ReqAPIList {
  apiKey: string;
  moduleName: string;
}

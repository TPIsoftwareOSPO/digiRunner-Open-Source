import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0434 extends BaseReq {
  ReqBody: Array<AA0434DTO>;
}

export interface AA0434DTO {
  apiKey: string;
  moduleName: string;
  orgID: string;
}

export interface RespAA0434 extends BaseRes {
  RespBody: Array<AA0434DTO>;
}

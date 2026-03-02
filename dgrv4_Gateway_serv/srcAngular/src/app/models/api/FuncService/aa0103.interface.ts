import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0103 extends BaseReq {
  ReqBody: AA0103Req;
}
export interface AA0103Req {
  funcCode?: string;
  locale?: string;
  keyword?: string;
  funcType: string; //0=功能維護  ,1 = 嵌入式頁面維護
}

export interface ResAA0103 extends BaseRes {
  RespBody: AA0103Resp;
}
export interface AA0103Resp {
  funcInfoList: Array<AA0103List>;
}
export interface AA0103List {
  funcCode: string;
  locale: string;
  funcName: string;
  funcNameEn: string;
  funcDesc: string;
  updateUser: string;
  updateTime: string;
  reportUrl?: string;
  funcType: string;
  masterFuncName?: string;
  localeName: string;
  reportType?: string;
}

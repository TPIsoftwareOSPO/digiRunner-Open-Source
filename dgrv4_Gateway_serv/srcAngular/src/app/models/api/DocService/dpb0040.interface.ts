import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0040 extends BaseReq {
  ReqBody: DPB0040Req;
}
export interface DPB0040Req {
  moduleName: string | null;
  moduleVersion: string | null;
  keyword?: number;
}

export interface ResDPB0040 extends BaseRes {
  RespBody: DPB0040Resp;
}
export interface DPB0040Resp {
  moduleList: Array<moduleList>;
}
export interface moduleList {
  moduleName: string;
  moduleVersion: string;
  deniedFlag: string;
  deniedString: string; // 前端顯示用
  id: number;
}

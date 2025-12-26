import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0174 extends BaseReq {
  ReqBody: DPB0174Req;
}

export interface DPB0174Req {
  longId: string;
  keyword: string;
}

export interface RespDPB0174 extends BaseRes {
  RespBody: DPB0174Resp;
}

export interface DPB0174Resp {
  dataList: Array<DPB0174RespItem>;
 }

 export interface DPB0174RespItem {
  hexId: string;
  longId: string;
  siteName: string;
  targetWs: string;
  changeDateTime: string;
  changeUser: string;
 }

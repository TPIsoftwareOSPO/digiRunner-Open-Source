import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0304 extends BaseReq {
  ReqBody: DPB0304Req;
}

export interface DPB0304Req {
  sortBy?: string;
  lastSyncId?: string;
  syncType?: string;
  status?: string;
  keyword?: string;
}

export interface RespDPB0304 extends BaseRes {
  RespBody: DPB0304Resp;
}

export interface DPB0304Resp {
  dataList: DPB0304RespItem[];

}

export interface DPB0304RespItem {
  syncId : string;
  syncType : string;
  scheduleId ?: string;
  sourceId : string;
  targetIds : string[];
  status : string;
  currentStep : string;
  startTime : number;
  endTime ?: number;
  duration : number;
  createUser : string;
}



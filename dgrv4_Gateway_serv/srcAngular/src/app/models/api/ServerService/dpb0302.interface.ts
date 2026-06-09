import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0302 extends BaseReq {
  ReqBody: DPB0302Req;
}

export interface DPB0302Req {}

export interface RespDPB0302 extends BaseRes {
  RespBody: DPB0302Resp;
}

export interface DPB0302Resp {
  nodeInfoList: DPB0302RespItem[];
  btnStatus: boolean;
  currentSync: DPB0302RespCurrentSyncInfo;
}

export interface DPB0302RespItem {
  nodeName: string;
  nodeId: string;
  role: string;
}

export interface DPB0302RespCurrentSyncInfo {
  syncType: string;
  syncId: string;
  status: string;
  progress: number;
  currentStep: string;
  sourceId: string;
  targetIds: string[];
  startTime: string;
  elapsed: number;
  scheduleId?: string;
}

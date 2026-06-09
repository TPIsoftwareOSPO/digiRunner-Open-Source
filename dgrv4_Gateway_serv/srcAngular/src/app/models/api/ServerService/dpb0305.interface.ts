import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0305 extends BaseReq {
  ReqBody: DPB0305Req;
}

export interface DPB0305Req {
  syncId: string;
}

export interface RespDPB0305 extends BaseRes {
  RespBody: DPB0305Resp;
}

export interface DPB0305Resp {
  syncId: string;
  syncType: string;
  scheduleId?: string;
  sourceId: string;
  targetIds: string[];
  status: string;
  currentStep: string;
  progress: number;
  startTime: string;
  endTime: string;
  duration: number;
  errorMessage?: string;
  createUser: string;
  createDateTime: string;
  updateDateTime: string;
}

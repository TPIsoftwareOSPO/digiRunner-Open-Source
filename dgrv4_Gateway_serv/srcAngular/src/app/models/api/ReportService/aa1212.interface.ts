import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1212 extends BaseReq {
  ReqBody: AA1212Req;
}

export interface AA1212Req {
}

export interface ResAA1212 extends BaseRes {
  RespBody: AA1212Resp;
}

export interface AA1212Resp {
  dataList?: Array<AA1212RespItem>;
  lastLoginLogList: Array<AA1212LastLoginLog>;
}

export interface AA1212RespItem {
  nodeName?: string;
  keeperServer?: string;
  ip?: string;
  updateTime?: string;
  totalRequest?: string;
  success?: AA1212SuccessResp;
  fail?: AA1212FailResp;
  badAttempt?: AA1212BadAttemptResp;
  badAttemptList?: Array<AA1212BadAttemptItemResp>;
  reqTps?: string;
  respTps?: string;
  inclundeFailFastList?: Array<AA1212RankedResp>;
  inclundeFailSlowList?: Array<AA1212RankedResp>;
  exclundeFailFastList?: Array<AA1212RankedResp>;
  exclundeFailSlowList?: Array<AA1212RankedResp>;
  db?: AA1212DbResp;
  cache?: AA1212CacheResp;
  queue?: AA1212QueueResp;
  nodeInfo?: AA1212NodeInfoResp;
  apiThreadStatus?: AA1212ApiThreadStatusResp;
}

export interface AA1212SuccessResp {
  success: string;
  total: string;
  percentage: string;
}

export interface AA1212FailResp {
  fail: string;
  total: string;
  percentage: string;
}

export interface AA1212BadAttemptResp {
  code401: string;
  code403: string;
  others: string;
}

export interface AA1212BadAttemptItemResp {
  uri: string;
  statusCode: string;
}

export interface AA1212RankedResp {
  uri: string;
  statusCode: number;
  elapsedTime: string;
}

export interface AA1212DbResp {
  total: number;
  active: number;
  idle: number;
  waiting: number;
}

export interface AA1212CacheResp {
  rcd: string;
  dao: string;
  fixed: string;
}

export interface AA1212QueueResp {
  es: string;
  rdb: string;
}

export interface AA1212NodeInfoResp {
  cpuCore: number;
  cpuUsage: string;
  memFree: string;
  memTotal: string;
  memMax: string;
}

export interface AA1212ApiThreadStatusResp {
  countryRoadActvieCount: string;
  countryRoadPoolSize: string;
  highwayActvieCount: string;
  highwayPoolSize: string;
}

export interface AA1212LastLoginLog {
  loginDate: string;
  loginIp: string;
  loginStatus: string;
}

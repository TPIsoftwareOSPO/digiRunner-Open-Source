import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1211 extends BaseReq {
  ReqBody: AA1211Req;
}
export interface AA1211Req {
  timeType: number;
}

export interface ResAA1211 extends BaseRes {
  RespBody: AA1211Resp;
}

export interface AA1211Resp {
  data: AA1211RespItem;
}

export interface AA1211RespItem {
  timeType: number;
  dataTime: string;
  request: string;
  success: AA1211SuccessResp;
  fail: AA1211FailResp;
  badAttempt: AA1211BadAttemptResp;
  avg: number;
  median: AA1211MedianResp;
  popular?: Array<AA1211PopularResp>;
  unpopular?: Array<AA1211UnpopularResp>;
  apiTrafficDistribution: Array<AA1211ApiTrafficDistributionResp>;
  clientUsagePercentage?: Array<AA1211ClientUsagePercentageResp>;
  lastLoginLog?:Array<AA1211LastLoginLog>
}

export interface AA1211LastLoginLog {
  loginDate: string;
  loginIP: string;
}
export interface AA1211SuccessResp {
  success: string;
  total: string;
  percentage: string;
}

export interface AA1211FailResp {
  fail: string;
  total: string;
  percentage: string;
}

export interface AA1211BadAttemptResp {
  code_401: string;
  code_403: string;
  others: string;
  total: string;
}

export interface AA1211MedianResp {
  min: number;
  max: number;
  median: number;
  gap?: number;
}

export interface AA1211PopularResp {
  rank: number;
  apiName: string;
  total: string;
  success: string;
  fail: string;
  avg: number;
}

export interface AA1211UnpopularResp {
  rank: number;
  apiName: string;
  total: string;
  floating: string;
}

export interface AA1211ApiTrafficDistributionResp {
  xLable: string;
  success: string;
  fail: string;
  sort: number;
}

export interface AA1211ClientUsagePercentageResp {
  client: string;
  percentage: string;
  request: string;
  total: string;
  apiUsage: Array<AA1211ClientUsagePercentageRespItem>;
}

export interface AA1211ClientUsagePercentageRespItem {
  apiName: string;
  total: string;
  success: string;
  fail: string;
  avg: number;
}

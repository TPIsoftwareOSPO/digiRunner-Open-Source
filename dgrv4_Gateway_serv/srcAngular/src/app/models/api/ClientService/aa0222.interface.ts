import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0222: 查詢GroupList
 * 在TSMP中查詢所有Group清單。
 */
export interface ReqAA0222 extends BaseReq {
  ReqBody: AA0222Req;
}
export interface AA0222Req {
  vgroupId?: string;
  keyword?: string;
  vgroupAuthoritiesIds?: Array<string>;
  securityLevelId?: string;

  //之後要刪掉
  vgroupName?: string;
  vgroupAlias?: string;
  vgroupAuthorities?: string;
  securityLevelID?: string;
}

export interface ResAA0222 extends BaseRes {
  RespBody: AA0222Resp;
}
export interface AA0222Resp {
  dataList: Array<VGroupInfo>

  //之後要刪掉
  listCount: number;
  vgroupInfoList: Array<VGroupInfo>;
}
export interface VGroupInfo {
  vgroupId: string;
  vgroupName: string;
  vgroupAlias: string;
  isDescTruncated: boolean;
  vgroupDesc: string;
  oriVgroupDesc: string;
  isTruncated: boolean;
  vgroupAuthorities: string;
  oriVgroupAuthorities: string;
  securityLevelName: string;
  createTime: string;
}

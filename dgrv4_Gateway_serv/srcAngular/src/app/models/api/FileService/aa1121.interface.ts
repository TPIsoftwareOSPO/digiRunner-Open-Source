import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA1121 extends BaseReq {
  ReqBody: RequestParam;
}

export interface RequestParam {
  file: File;
  req: string;
}

export interface RespAA1121 extends BaseRes {
  RespBody: AA1121Resp;
}

export interface AA1121Resp {
  lackApiList: Array<AA1121LackApi>;
  clientList: Array<AA1121Client>;
  groupList: Array<AA1121Group>;
  vgroupList: Array<AA1121Vgroup>;
  groupAuthList: Array<AA1121GroupAuth>;
  securityLevelList: Array<AA1121SecurityLevel>;
  rdbConnectionList: Array<AA1121RdbConnection>;
  longId: string;
}

export interface AA1121LackApi {
  moduleName: string;
  apiId: string;
}

export interface AA1121Client {
  dataStatus: string;
  clientId: string;
  clientName: string;
  clientAlias: string;
}

export interface AA1121Group {
  dataStatus: string;
  groupName: string;
  groupAlias: string;
}

export interface AA1121Vgroup {
  dataStatus: string;
  vgroupName: string;
  groupAlias: string;
}

export interface AA1121GroupAuth {
  dataStatus: string;
  groupAuthId: string;
  groupAuthName: string;
}

export interface AA1121SecurityLevel {
  dataStatus: string;
  securityLevelId: string;
  securityLevelName: string;
}

export interface AA1121RdbConnection {
  dataStatus: string;
  connectionName: string;
}

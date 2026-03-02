import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0144 extends BaseReq {
    ReqBody: DPB0144Req;
}
export interface DPB0144Req {

}

export interface RespDPB0144 extends BaseRes {
    RespBody: DPB0144Resp;
}
export interface DPB0144Resp {
  audit: DPB0144RespAudit;
  summary: DPB0144RespSummary;
  liveNodeList: Array<DPB0144RespLiveNode>;
  lostNodeList: Array<DPB0144RespLostNode>;
  liveComposerList: Array<DPB0144RespComposerNode>;
  period:string;
}

export interface DPB0144RespAudit {
  login: DPB0144RespAuditItemLogin;
  user: DPB0144RespAuditItem;
  role: DPB0144RespAuditItem;
  client: DPB0144RespAuditItem;
  group: DPB0144RespAuditItem;
  registerApi: DPB0144RespAuditItem;
  composerApi: DPB0144RespAuditItem;
}

export interface DPB0144RespAuditItemLogin {
  success:string;
}

export interface DPB0144RespAuditItem {
  create:string;
  delete:string;
  update:string;
}

export interface DPB0144RespSummary {
  users: DPB0144RespSummaryItem;
  clients: DPB0144RespSummaryItem;
  roles: string;
  groups: string;
  registerApis: DPB0144RespSummaryItemApis;
  composerApis: DPB0144RespSummaryItemApis;
}

export interface DPB0144RespSummaryItem {
  enabled: string;
  disabled: string;
  locked: string;
}

export interface DPB0144RespSummaryItemApis {
  on: string;
  off: string;
}

export interface DPB0144RespLiveNode {
  nodeName?: string;
  ip?: string;
  port?: number;
  startupTime?: string;
}

export interface DPB0144RespLostNode {
  nodeName?: string;
  ip?: string;
  port?: number;
  lostTime?: string;
}

export interface DPB0144RespComposerNode {
  composerID?: string;
  keeperServerIp?: string;
  remoteIP?: string;
  remotePort?: number;
  startupTime?: string;
  ts?:number;
  tsToString?: string;
  upTime?:string;
  version?: string;
  webServerPort?: string;
}

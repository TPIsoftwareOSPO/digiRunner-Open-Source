import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0291 extends BaseReq {
  ReqBody: DPB0291Req;
}

export interface DPB0291Req {
  enable: string;
  proxyIds: Array<string>;
}

export interface RespDPB0291 extends BaseRes {
  RespBody: DPB0291Resp;
}

export interface DPB0291Resp {
  gRPCProxyList:Array<DPB0291RespItem>;
}
export interface DPB0291RespItem {
  gRPCProxyMapId: string;
  serviceName: string;
  proxyHostName: string;
  targetHostName: string;
  targetPort: string;
  connectTimeoutMs: string;
  sendTimeoutMs: string;
  readTimeoutMs: string;
  enable: string;
}

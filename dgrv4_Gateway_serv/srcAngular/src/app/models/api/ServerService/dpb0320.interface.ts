import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0320 extends BaseReq {
  ReqBody: DPB0320Req;
}

export interface DPB0320Req {
  stickyList: SmartOnFhirProxyStickyUpdateDto[];
}

export interface SmartOnFhirProxyStickyUpdateDto {
  sofProxyStickyId: string;
  version: string;
}

export interface RespDPB0320 extends BaseRes {
  RespBody: DPB0320Resp;
}

export interface DPB0320Resp {
  successCount: number;
  deletedStickyIds: string[];
}

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0240 extends BaseReq {
  ReqBody: DPB0240Req;
}

export interface DPB0240Req {
  gtwIdpInfoCusId?: string;
  clientId: string;
  keyword?: string;
  status?: string;
}

export interface RespDPB0240 extends BaseRes {
  RespBody: DPB0240Resp;
}

export interface DPB0240Resp {
  infoList: Array<DPB0240RespItem>;
}

export interface DPB0240RespItem {
  gtwIdpInfoCusId: string;
  clientId: string;
  status: string;
  cusLoginUrl: string;
  cusUserDataUrl: string;
}

// export interface DPB0240RespBefore extends BaseRes {
//   RespBody: RespDPB0240RespBefore;
// }

// export interface RespDPB0240RespBefore {
//   constraints: Array<ValidatorFormat>;
// }

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0229 extends BaseReq {
  ReqBody: DPB0229Req;
}

export interface DPB0229Req {}

export interface RespDPB0229 extends BaseRes {
  RespBody: DPB0229Resp;
}

export interface DPB0229Resp {
  infoList: Array<DPB0229RespItem>;
}

export interface DPB0229RespItem {
  dgrMtlsClientCertId:string;
  hostAndPort: string;
  rootCAExpireDate: string;
  CRTExpireDate: string;
  updateDateTime: string;
  updateUser: string;
  enable: string;
}

// export interface DPB0229RespBefore extends BaseRes {
//   RespBody: RespDPB0229RespBefore;
// }

// export interface RespDPB0229RespBefore {
//   constraints: Array<ValidatorFormat>;
// }

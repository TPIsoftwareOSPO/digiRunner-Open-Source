import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0220 extends BaseReq {
  ReqBody: DPB0220Req;
}

export interface DPB0220Req {
  cusId: string;
  keyword: string;
  cusStatus: string;
}

export interface RespDPB0220 extends BaseRes {
  RespBody: DPB0220Resp;
}

export interface DPB0220Resp {
  infoList: Array<DPB0220RespItem>;
}

export interface DPB0220RespItem {
  cusId: string;
  cusName?: string;
  cusStatus: string;
  cusLoginUrl: string;
  cusBackendLoginUrl: string;
  cusUserDataUrl: string;
}

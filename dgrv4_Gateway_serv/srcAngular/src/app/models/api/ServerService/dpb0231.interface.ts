import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0231 extends BaseReq {
  ReqBody: DPB0231Req;
}

export interface DPB0231Req {
  host: string;
  port: string;
  rootCa: string;
  clientCert: string;
  clientKey: string;
  keyMima?: string;

}

export interface RespDPB0231 extends BaseRes {
  RespBody: DPB0231Resp;
}

export interface DPB0231Resp {
  msg: string;
  success: boolean;
}

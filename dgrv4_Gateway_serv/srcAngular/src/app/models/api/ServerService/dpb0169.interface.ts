import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0169 extends BaseReq {
  ReqBody: DPB0169Req;
}
export interface DPB0169Req {
  clientId: string;
}

export interface RespDPB0169 extends BaseRes {
  RespBody: DPB0169Resp;
}

export interface DPB0169Resp {
  dataList: Array<DPB0169RespItem>;
}

export interface DPB0169RespItem {
  id: string;
  longId: string;
  clientId: string;
  idpType: string;
  status: string;
  remark?: string;
  idpClientId: string;
  idpClientName: string;
  createDateTime: string;
  createUser: string;
  updateDateTime: string;
  updateUser: string;
}


import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0230 extends BaseReq {
  ReqBody: DPB0230Req;
}

export interface DPB0230Req {
  idList: Array<String>;
  enable: string;
}

export interface RespDPB0230 extends BaseRes {
  RespBody: DPB0230Resp;
}

export interface DPB0230Resp {
  enable: boolean;
}

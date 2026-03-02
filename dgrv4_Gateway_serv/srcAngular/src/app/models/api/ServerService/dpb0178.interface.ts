import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0178 extends BaseReq {
  ReqBody: DPB0178Req;
}

export interface DPB0178Req {
  longId: string;
}

export interface RespDPB0178 extends BaseRes {
  RespBody: DPB0178Resp;
}

export interface DPB0178Resp {

}

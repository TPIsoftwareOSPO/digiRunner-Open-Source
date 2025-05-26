import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0244 extends BaseReq {
  ReqBody: DPB0244Req;
}

export interface DPB0244Req {
  gtwIdpInfoCusId: string;
  clientId: string;
}

export interface RespDPB0244 extends BaseRes { }

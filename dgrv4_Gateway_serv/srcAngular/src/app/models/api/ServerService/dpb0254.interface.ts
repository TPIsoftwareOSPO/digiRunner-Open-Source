import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0254 extends BaseReq {
  ReqBody: DPB0254Req;
}

export interface DPB0254Req {}

export interface RespDPB0254 extends BaseRes {
  RespBody: DPB0254Resp;
}

export interface DPB0254Resp {}

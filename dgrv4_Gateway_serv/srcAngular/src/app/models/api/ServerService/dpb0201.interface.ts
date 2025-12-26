import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0201 extends BaseReq {
  ReqBody: DPB0201Req;
}

export interface DPB0201Req {
  websiteName: string;
}

export interface RespDPB0201 extends BaseRes {
  RespBody: DPB0201Resp;
}

export interface DPB0201Resp {
  itemList: Array<DPB0201RespItem>;

}

export interface DPB0201RespItem {
  targetUrl: string;
  req: number;
  resp: number;
}

import { BaseReq, BaseRes } from '../base.interface';


export interface ReqAA0435 extends BaseReq {
  ReqBody: AA0435Req;
}

export interface AA0435Req {
  type: string;
  field: Array<string>;
}

export interface RespAA0435 extends BaseRes {
  RespBody: AA0435Resp;
}

export interface AA0435Resp {

}

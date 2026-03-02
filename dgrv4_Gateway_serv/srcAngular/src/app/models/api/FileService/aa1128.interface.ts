import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA1128 extends BaseReq {
    ReqBody: AA1128Req;
}
export interface AA1128Req {
  longId:string;
}

export interface RespAA1128 extends BaseRes { }

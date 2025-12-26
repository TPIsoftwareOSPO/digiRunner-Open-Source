import { BaseReq, BaseRes } from "../base.interface";

export interface ReqAA1129 extends BaseReq {
    ReqBody: AA1129Req;
}
export interface AA1129Req {
  longId:string;
}

export interface RespAA1129 extends BaseRes { }

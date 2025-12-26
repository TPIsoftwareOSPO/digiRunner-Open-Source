import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0023 extends BaseReq {
    ReqBody: DPB0023Req;
}
export interface DPB0023Req {
    apiThemeId: number;
}

export interface RespDPB0023 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { }
import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0053 extends BaseReq {
    ReqBody: DPB0053Req;
}
export interface DPB0053Req {
    themeName: string;
    fileName: string;
}

export interface RespDPB0053 extends BaseRes {
    RespBody: DPB0053Resp;
}
export interface DPB0053Resp {
    themeId: number;
    fielId: number;
}
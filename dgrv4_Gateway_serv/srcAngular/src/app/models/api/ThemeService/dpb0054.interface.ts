import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0054 extends BaseReq {
    ReqBody: DPB0054Req;
}
export interface DPB0054Req {
    themeId: number;
    themeName: string;
    fileName?: string|null;
    lv: number;
}

export interface RespDPB0054 extends BaseRes {
    RespBody: DPB0054Resp;
}
export interface DPB0054Resp {
    themeId: number;
    fielId: number;
    lv: number;
}

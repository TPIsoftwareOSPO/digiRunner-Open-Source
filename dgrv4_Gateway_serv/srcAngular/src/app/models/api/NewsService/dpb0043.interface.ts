import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0043 extends BaseReq {
    ReqBody: DPB0043Req;
}
export interface DPB0043Req {
    newsId: number;
    lv: number;
    statusEncode?: string;
    newTitle?: string;
    newContent?: string;
    postDateTime: string;
    typeItemNo?: string;
}

export interface RespDPB0043 extends BaseRes {
    RespBody: DPB0043Resp;
}
export interface DPB0043Resp {
    newsId: number;
    lv: number;
}
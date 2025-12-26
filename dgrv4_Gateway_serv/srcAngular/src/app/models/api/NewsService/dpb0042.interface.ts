import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0042 extends BaseReq {
    ReqBody: DPB0042Req;
}
export interface DPB0042Req {
    newTitle: string;
    newContent: string;
    postDateTime: string;
    typeItemNo: string;
}

export interface RespDPB0042 extends BaseRes {
    RespBody: DPB0042Resp;
}
export interface DPB0042Resp {
    newsId: number;
}
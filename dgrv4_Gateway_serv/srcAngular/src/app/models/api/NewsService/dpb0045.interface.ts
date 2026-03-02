import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0045 extends BaseReq {
    ReqBody: DPB0045Req;
}
export interface DPB0045Req {
    newsId: number;
    fbTypeEncode: string;
}

export interface RespDPB0045 extends BaseRes {
    RespBody: DPB0045Resp;
}
export interface DPB0045Resp {
    newsId: number;
    newTitle: string;
    newContent: string;
    postDateTime: string;
    typeItemNo: string;
    typeItemName: string;
    lv: number;
    orgId: string;
    orgName: string;
}
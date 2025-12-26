import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0044 extends BaseReq {
    ReqBody: DPB0044Req;
}
export interface DPB0044Req {
    newsId?: number;
    keyword?: string;
    queryStartDate: string;
    queryEndDate: string;
    typeItemNo?: string;
    fbTypeEncode: string; // v3.6
    enFlagEncode: string; // v3.4
}

export interface RespDPB0044 extends BaseRes {
    RespBody: DPB0044Resp;
}
export interface DPB0044Resp {
    dataList: Array<DPB0044NewsItem>;
}
export interface DPB0044NewsItem {
    newsId: number;
    newTitle: string;
    newContent: string;
    newContentstr: string; // 前端顯示用
    status: string;
    statusName: string;
    postDateTime: string;
    typeItemNo: string;
    typeItemNoName: string;
    orgId: string;
    orgName: string;
    lv: number;
}
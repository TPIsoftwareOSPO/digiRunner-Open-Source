import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0056 extends BaseReq {
    ReqBody: DPB0056Req;
}
export interface DPB0056Req {
    themeId: number;
}

export interface RespDPB0056 extends BaseRes {
    RespBody: DPB0056Resp;
}
export interface DPB0056Resp {
    themeId: number;
    themeName: string;
    dataStatus: string;
    dataStatusName: string;
    dataSort: number;
    orgId: string;
    orgName: string;
    fileId: number;
    fileName: string;
    filePath: string;
    lv: number;
}
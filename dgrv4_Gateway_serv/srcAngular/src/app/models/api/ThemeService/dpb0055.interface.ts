import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0055 extends BaseReq {
    ReqBody: DPB0055Req;
}
export interface DPB0055Req {
    themeId?: number;
    keyword?: string;
}

export interface RespDPB0055 extends BaseRes {
    RespBody: DPB0055Resp;
}
export interface DPB0055Resp {
    dataList: Array<DPB0055Themes>;
}
export interface DPB0055Themes {
    themeId: number;
    themeName: string;
    dataStatus?: string;
    dataStatusName?: string;
    dataSort?: number;
    orgId?: string;
    orgName?: string;
    fileId?: number;
    fileName?: string;
    filePath?: string;
}
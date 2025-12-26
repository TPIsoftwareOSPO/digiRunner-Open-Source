import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0020 extends BaseReq {
    ReqBody: DPB0020Req;
}
export interface DPB0020Req {
    apiThemeId?: number;
    dataStatus: string;
    keyword?: string;
}

export interface RespDPB0020 extends BaseRes {
    RespBody: DPB0020Resp;
}
export interface DPB0020Resp {
    themeList: Array<DPB0020Theme>;
}
export interface DPB0020Theme {
    apiThemeId: number;
    apiThemeName: string;
    dataStatus: string;
    dataSort: number;
    orgApiList: Array<DPB0020API>;
}
export interface DPB0020API {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiStatus: string;
    apiSrc: string;
    apiDesc: string;
    apiUid: string;
}
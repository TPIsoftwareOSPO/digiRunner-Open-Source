import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0021 extends BaseReq {
    ReqBody: DPB0021Req;
}
export interface DPB0021Req {
    apiThemeId: number;
}

export interface RespDPB0021 extends BaseRes {
    RespBody: DPB0021Resp;
}
export interface DPB0021Resp {
    apiThemeName: string;
    dataStatus: string;
    dataSort: number;
    orgApiList: Array<DPB0021API>;
    fileName: string;
    iconFileContent: string;
}
export interface DPB0021API {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiStatus: string;
    apiSrc: string;
    apiDesc: string;
    apiUid: string;
}
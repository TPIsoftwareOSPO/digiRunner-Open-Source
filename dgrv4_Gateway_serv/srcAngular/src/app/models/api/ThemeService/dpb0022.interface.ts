import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0022 extends BaseReq {
    ReqBody: DPB0022Req;
}
export interface DPB0022Req {
    fileName?: string;
    apiThemeName: string;
    dataStatus: string;
    dataSort: string;
    useApis?: Array<string>;
    iconFileContent?: string;
    orgApiList: Array<string>;
    orgIcon: string;
    apiThemeId: number;
}

export interface RespDPB0022 extends BaseRes {
    RespBody: DPB0022Resp;
}
export interface DPB0022Resp {
    apiThemeName: string;
    dataStatus: string;
    dataSort: number;
    orgApiList: Array<DPB0022API>;
}
export interface DPB0022API {
    apiKey: string;
    moduleName: string;
    apiName: string;
    apiStatus: string;
    apiSrc: string;
    apiDesc: string;
    apiUid: string;
}
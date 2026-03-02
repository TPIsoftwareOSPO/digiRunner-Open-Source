import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0019 extends BaseReq {
    ReqBody: DPB0019Req;
}
export interface DPB0019Req {
    fileName?: string;
    apiThemeName: string;
    dataStatus: string;
    dataSort: string;
    useApis?: Array<string>;
    iconFileContent?: string;
}

export interface RespDPB0019 extends BaseRes {
    RespBody: RespBody;
}
export interface RespBody { }
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0015 extends BaseReq {
    ReqBody: DPB0015Req;
}
export interface DPB0015Req {
    appId: number;
    name: string;
    refAppCateId: number;
    useApis: Array<string>;
    icon: Array<string>; // ['fileName','fileData to base64]
    intro: string;
    author: string;
    introFiles: Array<object>;
    dataStatus: string;
    orgUseApis: Array<string>; // ['fileName','fileData to base64]
    orgIcon: string;
    orgIntroFiles: Array<string>; // ['fileName','fileData to base64]
}

export interface ResDPB0015 extends BaseRes {
    RespBody: DPB0015Resp;
}
export interface DPB0015Resp { }
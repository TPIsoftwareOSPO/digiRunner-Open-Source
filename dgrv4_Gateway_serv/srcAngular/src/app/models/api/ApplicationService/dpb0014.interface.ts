import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0014 extends BaseReq {
    ReqBody: DPB0014Req;
}
export interface DPB0014Req {
    appId: number;
}

export interface ResDPB0014 extends BaseRes {
    RespBody: DPB0014Resp;
}
export interface DPB0014Resp {
    name: string;
    refAppCateId: number;
    refAppCateName: string;
    intro: string;
    author: string;
    dataStatus: string;
    useApis: Array<string>;
    orgUseApis: Array<string>; // ['fileName','fileData to base64]
    orgIcon: string;
    orgIntroFiles: Array<string>; // ['fileName','fileData to base64]
    orgIconFileName: string; // ['fileName']
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0012 extends BaseReq {
    ReqBody: DPB0012Req;
}
export interface DPB0012Req {
    name: string;
    refAppCateId: number;
    useApis: Array<string>;
    icon: object; // {'fileName':'fileData to base64}
    intro: string;
    author: string;
    introFiles: Array<object>; // {'fileName':'fileData to base64}
    dataStatus: string;
}

export interface ResDPB0012 extends BaseRes {
    RespBody: DPB0012Resp;
}
export interface DPB0012Resp { }
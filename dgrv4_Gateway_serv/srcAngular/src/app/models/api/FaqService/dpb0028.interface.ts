import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0028 extends BaseReq {
    ReqBody: DPB0028Req;
}
export interface DPB0028Req {
    questionId: number;
    questionName: string;
    answerId: number;
    answerName: string;
    dataSort: number;
    fileName: string;
    fileContent: string; // base64 encoded
    orgFileName: string;
    orgFileId: number;
    dataStatus: string;
}

export interface ResDPB0028 extends BaseRes {
    RespBody: DPB0028Resp;
}
export interface DPB0028Resp { }
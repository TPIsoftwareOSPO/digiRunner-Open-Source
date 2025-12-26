import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0029 extends BaseReq {
    ReqBody: DPB0029Req;
}
export interface DPB0029Req {
    questionId: number;
}

export interface ResDPB0029 extends BaseRes {
    RespBody: DPB0029Resp;
}
export interface DPB0029Resp {
    questionId: number;
    questionName: string;
    answerId: number;
    answerName: string;
    dataSort: number;
    dataStatus: string;
    orgFileId: number;
    orgFileName: string;
}
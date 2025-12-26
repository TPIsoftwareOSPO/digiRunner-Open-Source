import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0026 extends BaseReq {
    ReqBody: DPB0026Req;
}
export interface DPB0026Req {
    questionName: string;
    dataSort: number;
    answerName: string;
    dataStatus: string;
    fileName: string;
    fileContent: string;
}

export interface ResDPB0026 extends BaseRes {
    RespBody: DPB0026Resp;
}
export interface DPB0026Resp {
    faqList: Array<faqList>;
}
export interface faqList {
    questionName: string;
    dataStatus: string;
    answerId: number;
    answerName: string;
    questionId: number;
    dataSort: number;
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0027 extends BaseReq {
    ReqBody: DPB0027Req;
}
export interface DPB0027Req {
    questionId: number|null;
    dataSort: number|null;
    keyword?: string;
    dataStatus: string;
}

export interface ResDPB0027 extends BaseRes {
    RespBody: DPB0027Resp;
}
export interface DPB0027Resp {
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

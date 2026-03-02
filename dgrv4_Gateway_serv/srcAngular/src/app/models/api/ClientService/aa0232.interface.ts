import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0232 extends BaseReq {
    ReqBody: AA0232Req;
}
export interface AA0232Req {
    logSeq?: string;
    keyword?: string;
    startTime: string; // YYYY-MM-DD HH:mm
    endTime: string; // YYYY-MM-DD HH:mm
    eventType?: string;
}

export interface ResAA0232 extends BaseRes {
    RespBody: AA0232Resp;
}
export interface AA0232Resp {
    clientLogList: Array<AA0232Log>;
}
export interface AA0232Log {
    agent: string;
    clientID: string;
    clientIP: string;
    creatTime: String;
    eventMsg: string;
    eventTime: string;
    eventType: string;
    isLogin: boolean;
    logSeq: string;
    txsn: string;
    userName: string;
}
import { BaseReq, BaseRes } from '../base.interface';


export interface ReqAA0505 extends BaseReq {
    ReqBody: AA0505Req;
}
export interface AA0505Req {
    id?: number;
    nodeTaskId: number;
    taskSrc: string; // Java、.NET
    keyword?: string;
    success?: string; // JOB_STATUS
}

export interface ResAA0505 extends BaseRes {
    RespBody: AA0505Resp;

}
export interface AA0505Resp {
    nodeTaskId: number;
    taskSrc: string;
    dataList: Array<AA0505Item>;
}
export interface AA0505Item {
    id: number;
    isCompetitiveIdTruncated: boolean;
    competitiveId: string;
    oriCompetitiveId: string;
    competitiveTime: string;
    node?: string;
    updateTime: string;
    success?: string;
    successName?: string;
    isErrorMsgTruncated: boolean;
    errorMsg?: string;
    oriErrorMsg?: String;
}
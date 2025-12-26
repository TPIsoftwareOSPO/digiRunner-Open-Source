import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0525 extends BaseReq {
    ReqBody: AA0525Req;
}
export interface AA0525Req {
    id?: number;
    keyword?: string;
    startTime: string; // yyyy-MM-dd HH:mm
    endTime: string; // yyyy-MM-dd HH:mm
    taskSrc: string; // Java、.NET
}

export interface ResAA0525Before extends BaseRes {
    RespBody: AA0525RespBefore;
}
export interface AA0525RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0525 extends BaseRes {
    RespBody: AA0525Resp;

}
export interface AA0525Resp {
    dataList: Array<AA0525Item>;
}
export interface AA0525Item {
    id: number;
    taskId: string;
    isTaskSignatureTruncated: boolean;
    taskSignature: string;
    oriTaskSignature: string;
    isTaskArgTruncated: boolean;
    taskArg: string;
    oriTaskArg: string;
    coordination: string;
    noticeNode: string;
    noticeTime: string;
    executeTime: string;
}
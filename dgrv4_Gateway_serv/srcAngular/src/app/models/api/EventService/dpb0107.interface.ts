import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0107 extends BaseReq {
    ReqBody: DPB0107Req;
}
export interface DPB0107Req {
    eventId: number;
}

export interface ResDPB0107 extends BaseRes {
    RespBody: DPB0107Resp;
}
export interface DPB0107Resp {
    eventId: number;
    eventTypeId: string;
    eventTypeName: string;
    eventNameId: string;
    eventName: string;
    moduleName: string;
    moduleVersion: string;
    traceId: string;
    infoMsg: string;
    archiveFlag: string;
    keepFlag: string;
    nodeAlias: string;
    nodeId: string;
    threadName: string;
    createDateTime: string;
    createUser: string;
}
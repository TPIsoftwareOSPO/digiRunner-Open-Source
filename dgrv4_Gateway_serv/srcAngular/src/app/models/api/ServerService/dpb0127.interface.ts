import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0127 extends BaseReq {
    ReqBody: DPB0127Req;
}

export interface DPB0127Req {
    startDate: string;
    endDate: string;
    keywords?: string; //關鍵字
    auditLogId?: string; //pk
    auditExtId?: string;
}

export interface RespDPB0127 extends BaseRes {
    RespBody: DPB0127Resp;
}

export interface DPB0127Resp {
    dataList:Array<DPB0127RespItem>;
}

export interface DPB0127RespItem {
    auditLongId: any;
    txnUid: string;
    userName: string;
    clientId: string;
    apiUrl: string;
    origApiUrl: string;
    eventNo: string;
    eventName: string;
    userIp: string;
    userHostname: string;
    userRole: string;
    param1: string;
    param2: string;
    param3: string;
    param4: string;
    param5: string;
    stackTrace: string;
    isUserRoleTruncated: boolean|null;
    truncatedUserRole: string;
    createDateTime: string;
    auditExtId: string;
}

export interface ReqDPB0127Before extends BaseRes {
    RespBody: ReqDPB0127RespBefore;
}
export interface ReqDPB0127RespBefore {
    constraints: Array<ValidatorFormat>;
}

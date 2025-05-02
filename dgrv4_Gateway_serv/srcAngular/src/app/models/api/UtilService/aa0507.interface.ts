import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0507: getAuditLog
 * 取得稽核紀錄。稽核紀錄即使用者在TSMP Admin Console上的操作紀錄，也就是API Log中屬於TSMPAA的API Log。
 */
export interface ReqAA0507 extends BaseReq {
    ReqBody: AA0507Req;
}
export interface AA0507Req {
    from: number;
    collectionDate?: string;
    id?: string;
    timeStart: string; // yyyy-MM-dd HH:mm
    timeEnd: string; // yyyy-MM-dd HH:mm
    txID?: string;
    keyword?: string;
}

export interface ResAA0507 extends BaseRes {
    RespBody: AA0507Resp;
}
export interface AA0507Resp {
    collectionDate: string;
    auditLogList: Array<AA0507AuditLog>;
}
export interface AA0507AuditLog {
    id: string;
    cip: string;
    httpHeader: string;
    mBody: string;
    mType: string;
    rCode: string;
    rMsg: string;
    txId: string;
    type: string;
    user: string;
    txsn: string;
    txDate: string;
    cId: string;
    ts:string; // yyyy-MM-dd HH:mm:ss.SSS
}

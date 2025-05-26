import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0128 extends BaseReq {
    ReqBody: DPB0128Req;
}

export interface DPB0128Req {
    txnUid: string;    
}

export interface RespDPB0128 extends BaseRes {
    RespBody: DPB0128Resp;
}

export interface DPB0128Resp { 
    dataList:Array<DPB0128RespItem>;
}

export interface DPB0128RespItem {
    auditLongId: any;
    txnUid: string;
    entityName: string;
    cud: string;
    cudName: string;
    param1: string;
    param2: string;
    param3: string;
    param4: string;
    param5: string;
    stackTrace: string;
    oldRowContent: string;
    oldRowBlob: string;
    newRowContent: string;
    newRowBlob: string;
}

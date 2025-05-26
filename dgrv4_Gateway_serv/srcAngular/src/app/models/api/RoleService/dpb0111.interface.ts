import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0111 extends BaseReq {
    ReqBody: DPB0111Req;
}
export interface DPB0111Req {
    p_roleId?: string;
    p_listType?: string;
    keyword?: string;
    listType?: string;
}

export interface RespDPB0111 extends BaseRes {
    RespBody: DPB0111Resp;
}
export interface DPB0111Resp {
    dataList: Array<DPB0111Item>;
}
export interface DPB0111Item {
    roleId: string;
    roleName: string;
    roleAlias: string;
    txId: string;
    isTxIdTruncated: boolean;
    oriTxId: string;
    listType: string;
    listTypeName: string;
}
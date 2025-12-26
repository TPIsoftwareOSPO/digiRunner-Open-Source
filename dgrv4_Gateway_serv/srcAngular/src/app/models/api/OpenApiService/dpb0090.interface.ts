import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0090 extends BaseReq {
    ReqBody: DPB0090Req;
}
export interface DPB0090Req {
    openApiKeyId?: number;
    clientId: string;
}

export interface ResDPB0090 extends BaseRes {
    RespBody: DPB0090Resp;
}
export interface DPB0090Resp {
    dataList: Array<DPB0090RespItem>;
}
export interface DPB0090RespItem {
    openApiKeyId: number;
    clientId: string;
    openApiKey: string;
    openApiKeyAlias: string;
    createDateTime: string;
    expiredAt: string;
    revokedAt: string;
    openApiKeyStatus: string;
    openApiKeyStatusName: string;
    queryVisiable: string;
    updateVisiable: string;
    revokeVisiable: string;
} 
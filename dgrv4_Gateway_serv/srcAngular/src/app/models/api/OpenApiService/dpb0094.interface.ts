import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0094 extends BaseReq {
    ReqBody: DPB0094Req;
}
export interface DPB0094Req {
    openApiKeyId?: number;
    startDate: string;
    endDate: string;
    keyword?: string;
}

export interface RespDPB0094Before extends BaseRes {
    RespBody: DPB0094RespBefore;
}
export interface DPB0094RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface RespDPB0094 extends BaseRes {
    RespBody: DPB0094Resp;
}
export interface DPB0094Resp {
    dataList: Array<DPB0094RespItem>;
}
export interface DPB0094RespItem {
    openApiKeyId: number;
    clientId: string;
    clientName: string;
    clientAlias: string;
    openApiKey: string;
    openApiKeyAlias: string;
    createDateTime: string;
    expiredAt: string;
    revokedAt: string;
} 
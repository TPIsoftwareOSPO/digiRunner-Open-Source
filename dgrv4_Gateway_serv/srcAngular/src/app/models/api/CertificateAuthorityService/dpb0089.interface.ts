import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0089 extends BaseReq {
    ReqBody: DPB0089Req;
}
export interface DPB0089Req {
    ids: Array<number>;
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0089 extends BaseRes {
    RespBody: DPB0089Resp;
}
export interface DPB0089Resp {
    fileContentStr: string;
}
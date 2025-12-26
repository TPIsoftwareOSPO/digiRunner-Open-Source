import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0087 extends BaseReq {
    ReqBody: DPB0087Req;
}
export interface DPB0087Req {
    ids: Array<number>;
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0087 extends BaseRes {
    RespBody: DPB0087Resp; // Binary file data
}
export interface DPB0087Resp { } 
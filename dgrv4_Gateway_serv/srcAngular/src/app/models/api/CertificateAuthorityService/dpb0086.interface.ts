import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0086 extends BaseReq {
    ReqBody: DPB0086Req;
}
export interface DPB0086Req {
    clientId: string;
    clientCertId?: number; // JWE
    clientCert2Id?: number // TLS
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE 使用 (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0086 extends BaseRes {
    RespBody: DPB0086Resp;
}
export interface DPB0086Resp { } 
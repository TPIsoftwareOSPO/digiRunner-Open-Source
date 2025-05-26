import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0085 extends BaseReq {
    ReqBody: DPB0085Req;
}
export interface DPB0085Req {
    clientId: string;
    lockVersions?: object; // { clientCertId: lv } || { clientCertId2: lv }
    fileContent: string;
    fileName: string;
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0085 extends BaseRes {
    RespBody: DPB0085Resp;
}
export interface DPB0085Resp { } 
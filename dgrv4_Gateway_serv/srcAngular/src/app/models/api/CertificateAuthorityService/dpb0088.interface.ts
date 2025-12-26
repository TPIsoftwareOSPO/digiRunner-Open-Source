import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0088 extends BaseReq {
    ReqBody: DPB0088Req;
}
export interface DPB0088Req {
    clientCertId?: number; // JWE
    clientCert2Id?: number; // TLS
    startDate: string; // yyyy/mm/dd
    endDate: string; // yyyy/mm/dd
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0088 extends BaseRes {
    RespBody: DPB0088Resp;
}
export interface DPB0088Resp {
    certList: Array<DPB0088certItem>;
}
export interface DPB0088certItem {
    clientCertId?: number;
    clientCert2Id?: number;
    clientId: string;
    pubKey: string; // 公鑰
    certVersion?: string; // 憑證版本
    certSerialNum: string; // 憑證序號
    sAlgorithmID?: string; // 簽章演算法
    algorithmID: string; // 公鑰演算法
    certThumbprint: string; // CA數位指紋
    iuId?: string; // 發行方ID
    issuerName: string; // 發行方名稱
    sUid?: string; // 持有憑證者ID
    createAt: string; // 憑證創建日
    expiredAt: string; // 憑證到期日
    clientName: string;
    clientAlias: string;
    certFileName: string;
    createDateTime: string;
    createUser: string;
    updateDateTime: string;
} 
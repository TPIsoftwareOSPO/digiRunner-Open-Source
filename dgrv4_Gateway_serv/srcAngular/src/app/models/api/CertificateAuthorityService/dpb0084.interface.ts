import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0084 extends BaseReq {
    ReqBody: DPB0084Req;
}
export interface DPB0084Req {
    clientId: string;
    encodeCertType: string; // 使用BcryptParam，ITEM_NO='CERT_TYPE'，JWE (使用 TSMP_CLIENT_CERT TABLE)，TLS (使用 TSMP_CLIENT_CERT2 TABLE)
}

export interface ResDPB0084 extends BaseRes {
    RespBody: DPB0084Resp;
}
export interface DPB0084Resp {
    certList: Array<DPB0084certItem>;
}
export interface DPB0084certItem {
    clientCertId?: number;
    clientCert2Id?: number;
    clientId: string;
    clientFileName: string; // 檔名
    clientFileContent: string; // Base64 檔案內容
    pubKey: string; // 公鑰
    certVersion?: string; // 憑證版本
    certSerialNum: string; // 憑證序號
    sAlgorithmID?: string; // 簽章演算法
    alogorithmID: string; // 公鑰演算法
    certThumbprint: string; // CA數位指紋
    iuId?: string; // 發行方ID
    issuerName: string; // 發行方名稱
    sUid?: string; // 持有憑證者ID
    createAt: number; // 憑證創建日
    expiredAt: number; // 憑證到期日
    createDateTime: string;
    createUser: string;
    lv: number;
    keySize: number;
} 
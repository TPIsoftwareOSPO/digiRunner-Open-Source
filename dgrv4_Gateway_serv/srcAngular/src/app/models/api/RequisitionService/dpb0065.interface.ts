import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0065 extends BaseReq {
    ReqBody: DPB0065Req;
}
export interface DPB0065Req {
    reqType: string;
    reqSubtype?: string;
    reqDesc: string;
    effectiveDate: string;
    apiApplicationD?: DPB0065ApiApplication; // 用戶授權API
    apiOnOffD?: DPB0065ApiOnOff; // API上下架
    clientRegD?: DPB0065ClientReg; // 用戶註冊
    openApiKeyD?: DPB0065OpenApiKey; // Open API Key
}
/**
 * 用戶授權API
 */
export interface DPB0065ApiApplication {
    clientId: string;
    apiUids: Array<string>;
    tmpFileName?: string; // 暫存檔名
}
/**
 * API上下架
 */
export interface DPB0065ApiOnOff {
    apiUidDatas: object; // Map<string, Array<DPB0065ApiBindingData>>
    apiMapFileName?: object; // Map<apiUid,fileName>;
    encPublicFlag: string; //  BcryptParam 加密, ITEM_NO = 'API_AUTHORITY'
}
export interface DPB0065ApiBindingData {
    apiUid: string;
    refThemeId: number;
}
export interface newApiOnOffAddNo extends newApiOnOff {
    no: number;
}
export interface newApiOnOff {
    apiUid: string;
    apiName: string; // 顯示在 input 用
    refThemeId: Array<number>;
    refThemeName: Array<string>; // 顯示在 input 用
    fileName: string|null;
    tempFileName: string|null;
}
/**
 * 用戶註冊
 */
export interface DPB0065ClientReg {
    clientId: string;
    clientName: string;
    emails?: string; // 可以多組mail並請用，區隔
    clientBlock: string; // 最少6碼
    encPublicFlag: string; // 使用Bcrypt設計, itemNo = 'API_AUTHORITY'
    tmpFileName?: string; // 暫存檔名
}
/**
 * Open API Key
 */
export interface DPB0065OpenApiKey {
    clientId: string;
    openApiKeyId?: number;
    openApiKey?: string;
    secretKey?: string;
    openApiKeyAlias: string;
    timesThreshold: number;
    expiredAt: string; // YYYY/MM/DD
    apiUids: Array<string>; // API UUID
}

export interface ResDPB0065 extends BaseRes {
    RespBody: DPB0065Resp;
}
export interface DPB0065Resp {
    apiApplication?: DPB0065RespApiApplication; // 用戶授權API
    apiOnOff?: DPB0065RespApiOnOff; // API上下架
    clientReq?: DPB0065RespClientReg; // 用戶註冊
    openApiKey?: DPB0065RespOpenApiKey // Open API Key
}
/**
 * 用戶授權API
 */
export interface DPB0065RespApiApplication {
    reqOrdermId: number;
    lv: number;
    reqOrdersId: number;
    reqOrderd1Ids: Array<number>;
    fileId?: string;
    fileName?: string;
}
/**
 * API上下架
 */
export interface DPB0065RespApiOnOff {
    reqOrdermId: string;
    reqOrdersIds: Array<number>;
    reqOrderd2Ids: Map<number, Array<number>>;
    chkLogId: number;
    reqOrderd2FileIds: Map<number, number>;
}
/**
 * 用戶註冊
 */
export interface DPB0065RespClientReg {
    reqOrdermId: number;
    lv: number;
    reqOrdersId: number;
    reqOrderd3Id: number;
    fileId?: number;
    fileName?: string;
}
/**
 * Open API Key
 */
export interface DPB0065RespOpenApiKey {
    reqOrdermId: number;
    lv: number;
    reqOrdersId: number;
    reqOrderd5Id: number;
    reqOrderd5dIds: Array<number>;
}


export interface ResDPB0065Before extends BaseRes {
    RespBody: DPB0065RespBefore;
}

export interface DPB0065RespBefore {
    constraints: Array<ValidatorFormat>;
}

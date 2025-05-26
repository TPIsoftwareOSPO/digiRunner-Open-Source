import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';
import { DPB0065ApiBindingData } from './dpb0065.interface';

export interface ReqDPB0066 extends BaseReq {
    ReqBody: DPB0066Req;
}
export interface DPB0066Req {
    reqOrdermId: number;
    lv: number;
    act: string; // S: 送審; U: 更新; R: 重送
    reqDesc: string; // S: 送審時不用帶入
    effectiveDate: string; // S: 送審時不用帶入
    apiApplicationD?: DPB0066ApiApplication; // 用戶授權API，S: 送審時不用帶入
    apiOnOffD?: DPB0066ApiOnOff; //  { "apiUidDatas" : { "UUID0" : [{ "apiUid" : "UUID0", "refThemeId" : 0 }, { "apiUid" : "UUID0", "refThemeId" : 1 }] } API上下架，S: 送審時不用帶入
    clientRegD?: DPB0066ClientReg; // 用戶註冊，S: 送審時不用帶入
    openApiKeyD?: DPB0066OpenApiKey; // Open API Key，S: 送審時不用帶入
}
/**
 * 用戶授權API
 */
export interface DPB0066ApiApplication {
    newFileName?: string; // 若有更新則帶入 .wait temp file，若無異動則帶入與原檔相同(oriFileName)
    oriFileName?: string; // 原始檔
    apiUids: Array<string>;
    clientId: string;
}
/**
 * API上下架
 */
export interface DPB0066ApiOnOff {
    apiUidDatas: object; // { apiUid: [themeId, themeId] }
    oriApiMapFileName: object; // { apiUid: fileName } 舊檔案
    newApiMapFileName: object; // { apiUid: fileName } 新檔案 - 更新
    encPublicFlag: string; //  BcryptParam 加密, ITEM_NO = 'API_AUTHORITY'
}
export interface DPB0066ApiBindingData extends DPB0065ApiBindingData { }
/**
 * 用戶註冊
 */
export interface DPB0066ClientReg {
    clientId: string;
    clientName: string;
    emails?: string;
    clientBlock: string;
    encPublicFlag: string;
    newFileName?: string;
    oriFileName?: string;
}
/**
 * Open API Key
 */
export interface DPB0066OpenApiKey {
    clientId: string;
    openApiKeyId?: number;
    openApiKey?: string;
    secretKey?: string;
    openApiKeyAlias: string;
    timesThreshold: number;
    expiredAt: string;
    apiUids: Array<string>;
}

export interface ResDPB0066 extends BaseRes {
    RespBody: DPB0066Resp;
}
export interface DPB0066Resp {
    apiApplication: Array<DPB0066RespApiApplication>; // 用戶授權API
    apiOnOff?: Array<DPB0066RespApiOnOff>; // API上下架
    clientReg?: Array<DPB0066RespClientReg>;
    openApiKey?: Array<DPB0066RespOpenApiKey>
}
/**
 * 用戶授權API
 */
export interface DPB0066RespApiApplication {
    reqOrdermId: number;
    lv: number;
    reqOrdersIds: Array<number>;
    reqOrderd1Ids: Array<number>;
    apiAuthIds: Array<number>;
    fileId?: number;
    fileName?: string;
}
/**
 * API上下架
 */
export interface DPB0066RespApiOnOff {
    reqOrdermId: number;
    lv: number;
    apiFileName: object; // { apiUid: fileName }
    reqOrdersIds: Array<number>;
    reqOrderd2Ids: object; // { "10003" : [ 7, 8 ] }
}
/**
 * 用戶註冊
 */
export interface DPB0066RespClientReg {
    reqOrdermId: number;
    lv: number;
    reqOrdersIds: Array<number>;
    reqOrderd3Id: number;
    clientSeqId: number;
    fileId?: number;
    fileName?: string;
}
/**
 * Open API Key
 */
export interface DPB0066RespOpenApiKey {
    reqOrdermId: number;
    lv: number;
    reqOrdersIds: Array<number>;
    reqOrderd5Id: number;
    reqOrderd5dIds: Array<number>;
}


export interface ResDPB0066Before extends BaseRes {
    RespBody: DPB0066RespBefore;
}

export interface DPB0066RespBefore {
    constraints: Array<ValidatorFormat>;
}
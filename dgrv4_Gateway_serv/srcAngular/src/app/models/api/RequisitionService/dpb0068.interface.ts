import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0068 extends BaseReq {
    ReqBody: DPB0068Req;
}
export interface DPB0068Req {
    reqOrdermId: number;
    encodeQuyType: string; // REQ: 申請單, EXA: 待審單, REV: 已審單
}

export interface ResDPB0068 extends BaseRes {
    RespBody: DPB0068Resp;
}
export interface DPB0068Resp {
    reqOrdermId: number;
    reqType: string;
    title: string;
    reqOrderNo: string;
    createDateTime: string;
    applyUserName: string;
    orgName: string;
    subTitle: string;
    reqSubtype: string;
    chkStatus: string;
    chkStatusName: string;
    nextChkPoint: string; // 下一關卡 1、2、3...
    chkPointName: string;
    lv: number;
    reqDesc: string;
    effectiveDate: string;
    apiUserApply?: DPB0068ApiUserApply; // 用戶授權API
    apiOnOff?: DPB0068ApiOnOff; // API上下架
    clientReg?: DPB0068ClientReg; // 用戶註冊
    openApiKey?: DPB0068OpenApiKey;
}
/**
 * 用戶授權API
 */
export interface DPB0068ApiUserApply {
    clientId: string;
    clientName: string;
    clientAlias?: string;
    apiList: Array<DPB0068D1>;
    fileName?: string;
    filePath?: string; //檔名含路徑
}
export interface DPB0068D1 {
    reqOrderd1Id: number;
    apiUid: string;
    apiName: string;
    moduleName: string;
    orgName: string;
    themeList?: object; // { "0" : "政治", "1" : "交通", "2" : "娛樂" }
    apiDesc?: string;
    docFileInfo: object; // { "API說明文件.txt" : "API_ATTACHMENT/10001/API說明文件.txt" }
    apiKey: string;
    orgId: string;
    apiExtId?: number;
    dpStatus?: number;
    publicFlag?: string;
    publicFlagName?: string;
}
/**
 * API上下架
 */
export interface DPB0068ApiOnOff {
    publicFlag: string;
    publicFlagName: string;
    apiOnOffList: Array<DPB0068D2>;
}
export interface DPB0068D2 {
    reqOrderd2Id: number;
    apiName: string;
    apiUid: string;
    themeList: object; // { "0" : "政治", "1" : "交通", "2" : "娛樂" }
    docFileInfo: object; // { "API說明文件.txt" : "API_ATTACHMENT/10001/API說明文件.txt" }
    moduleName: string;
    apiDesc?: string;
    orgName?: string;
    apiKey: string;
    orgId: string;
    apiExtId?: number;
    dpStatus?: string;
    publicFlag?: string;
    publicFlagName?: string;
}
/**
 * 用戶註冊
 */
export interface DPB0068ClientReg {
    reqOrderd3Id: number;
    clientId: string;
    clientName: string;
    emails?: string;
    publicFlag: string;
    publicFlagName: string;
    fileName?: string;
    filePath?: string;
}
/**
 * Open API Key
 */
export interface DPB0068OpenApiKey {
    reqOrderd5Id: string;
    clientId: string;
    clientName: string;
    clientAlias: string;
    openApiKeyId?: number;
    openApiKey?: string;
    secretKey?: string;
    openApiKeyAlias: string;
    expiredAt: string;
    timesThreshold: number;
    apiDatas: Array<DPB0068D5>;
}
export interface DPB0068D5 {
    reqOrderd5dId: number;
    apiKey: string;
    moduleName: string;
    apiName: string;
    themeList: string;
    orgId: string;
    orgName: string;
    apiDesc: string;
    apiExtId: number;
    apiUid: string;
    docFileInfo?: object; // [{ fileName: filePath }]
}
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA1002: 查詢TOrgList
 * 在TSMP中查詢TSMP裡的組織單位清單。預設查詢所有TOrgList。
 */
export interface ReqAA1002 extends BaseReq {
    ReqBody: AA1002Req;
}
export interface AA1002Req {
    orgID?: string;
}

export interface ResAA1002 extends BaseRes {
    RespBody: AA1002Resp;
}
export interface AA1002Resp {
    orgList: AA1002List[];
}
export interface AA1002List {
    orgID: string;
    orgCode: string;
    orgName: string;
    parentID: string;
}
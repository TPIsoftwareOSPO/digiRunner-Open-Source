import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0202: 查詢ClientList
 * 在TSMP中查詢Client清單。
 */
export interface ReqAA0202 extends BaseReq {
    ReqBody: AA0202Req;
}
export interface AA0202Req {
    clientId?: string;
    keyword?: string;
    groupID?: string;
    encodeStatus: string; // ITEM_NO='ENABLE_FLAG' 1=啟用, 2=停用, 3=鎖定, -1=全部
}

export interface ResAA0202 extends BaseRes {
    RespBody: AA0202Resp;
}
export interface AA0202Resp {
    clientInfoList: Array<AA0202List>;
}
export interface AA0202List {
    clientId: string;
    clientName: string;
    clientAlias?: string;
    securityLevelId: string;
    securityLevelName: string;
    status: string;
    statusName: string;
    publicFlag: string; // 0: 對內 & 對外、1: 對外、2: 對內
    publicFlagName: string; // 0: 對內 & 對外、1: 對外、2: 對內
}

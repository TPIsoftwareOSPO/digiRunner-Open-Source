import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0201: 新增Client
 * 在TSMP中新增Client (Channel)，Client可取得token再呼叫TSMP中部署的API。
 * 新增成功時返回clientID。
 */
export interface ReqAA0201 extends BaseReq {
    ReqBody: AA0201Req;
}
export interface AA0201Req {
    clientID?: string;
    clientName: string;
    clientAlias: string;
    clientBlock: string;
    emails?: string;
    groupIDList?: Array<string>;
    hostList?: Array<AA0201HostReq>;
    tps?: number;
    encodeStatus: string; // ITEM_NO='ENABLE_FLAG'，1=啟用, 2=停用
    status: string;
    owner: string;
    signupNum?: string;
    // clientSD?: string; // YYYY-MM-DD
    // clientED?: string; // YYYY-MM-DD
    // svcST?: string; // 0000
    // svcET?: string; // 2359
    apiQuota?: string;
    apiUsed?: string;
    cPriority?: string; // 用戶優先權
    publicFlag: string; // ITEM_NO='API_AUTHORITY'，0: 對內 & 對外、1: 對外、2: 對內,
    remark: string;
    clientStartDate?: string;
    clientEndDate?: string;
    clientStartTimePerDay?: string;
    clientEndTimePerDay?: string;
    timeZone?: string;
}
export interface AA0201HostReq {
    hostName: string;
    hostIP: string;
}
/**
 * 前端自用
 */
export interface AA0201HostReqAddNo extends AA0201HostReq {
    no: number;
}

export interface ResAA0201Before extends BaseRes {
    RespBody: AA0201RespBefore;
}
export interface AA0201RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0201 extends BaseRes {
    RespBody: AA0201Resp;
}
export interface AA0201Resp {
    clientID: string;
}

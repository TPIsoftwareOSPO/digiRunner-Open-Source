import { AA0201HostReq } from './aa0201.interface';
import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

/**
 * AA0204: 更新Client
 * 在TSMP中更新Client資料。
 */
export interface ReqAA0204 extends BaseReq {
    ReqBody: AA0204Req;
}
export interface AA0204Req {
    clientID: string;
    clientName: string;
    newClientName: string;
    clientAlias: string;
    newClientAlias?: string;
    emails: string;
    newEmails?: string;
    groupIDList?: Array<string>;
    newGroupIDList?: Array<string>;
    hostList?: Array<AA0204Host>;
    newHostList?: Array<AA0201HostReq>;
    tps?: string;
    newTps?: string;
    owner?: string;
    newOwner: string;
    signupNum?: string;
    newSignupNum?: string;
    // clientSD?: string;
    // newClientSD?: string;
    // clientED?: string;
    // newClientED?: string;
    // svcST?: string;
    // newSvcST?: string;
    // svcET: string;
    // newSvcET?: string;
    apiQuota?: string;
    newApiQuota?: string;
    cPriority: string; //用戶優先權
    newCPriority: string; //用戶優先權
    encodePublicFlag: string; // 0: 對內 & 對外、1: 對外、2: 對內
    newEncodePublicFlag: string; // 0: 對內 & 對外、1: 對外、2: 對內,
    remark: string;
    newRemark: string;
    clientStartDate?: string;
    newClientStartDate?: string;
    clientEndDate?: string;
    newClientEndDate?: string;
    clientStartTimePerDay?: string;
    newClientStartTimePerDay?: string;
    clientEndTimePerDay?: string;
    newClientEndTimePerDay?: string;
    timeZone?: string;
    newTimeZone?: string;
}
export interface AA0204Host {
    hostSeq: number;
    hostName: string;
    hostIP: string;
}

export interface ResAA0204Before extends BaseRes {
    RespBody: AA0204RespBefore;
}
export interface AA0204RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0204 extends BaseRes { }

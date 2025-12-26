import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0203: 查詢ClientDetail
 * 在TSMP中查詢Client詳細資料。
 */
export interface ReqAA0203 extends BaseReq {
    ReqBody: AA0203Req;
}
export interface AA0203Req {
    clientID: string;
    clientName: string;
}

export interface ResAA0203 extends BaseRes {
    RespBody: AA0203Resp;
}
export interface AA0203Resp {
    clientID: string;
    clientName: string;
    clientAlias: string;
    emails: string;
    groupInfoList: Array<AA0203GroupInfo>;
    vgroupInfoList: Array<AA0203VgroupInfo>;
    hostList: Array<AA0203Host>;
    tps: string;
    status: string;
    owner: string;
    signupNum: string;
    // clientSD: string;
    // clientED: string;
    // svcST: string;
    // svcET: string;
    apiQuota: string;
    apiUsed: string;
    cPriority: string;
    createDate: string;
    createUser: string;
    updateDate: string;
    updateUser: string;
    securityLV: AA0203SecurityLV;
    pwdFailTimes: string;
    failTreshhold: string;
    publicFlag: string; // 0: 對內 & 對外、1: 對外、2: 對內,
    remark: string;
    clientStartDate?: string;
    clientEndDate?: string;
    clientStartTimePerDay?: string;
    clientEndTimePerDay?: string;
    timeZone?: string;
}
export interface AA0203GroupInfo {
    groupAlias: string;
    groupDesc: string;
    groupID: string;
    groupName: string;
    securityLevelID: string;
    securityLevelName: string;
}
export interface AA0203VgroupInfo {
    securityLevelID: string;
    securityLevelName: string;
    vgroupAlias: string;
    vgroupDesc: string;
    vgroupID: string;
    vgroupName: string;
}
export interface AA0203Host {
    hostSeq: number;
    hostName: string;
    hostIP: string;
}
export interface AA0203SecurityLV {
    securityLevelDesc: string;
    securityLevelId: string;
    securityLevelName: string;
}

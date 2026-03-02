import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA1001: 新增TOrg
 * 在TSMP Admin中新增TSMP組織單位，簡稱addTOrg。新增成功時返回ORG_ID。
 */
export interface ReqAA1001 extends BaseReq {
    ReqBody: AA1001Req;
}
export interface AA1001Req {
    parentId: string;
    orgName: string;
    orgCode: string;
    contactTel: string;
    contactName: string;
    contactMail: string;
}

export interface ResAA1001 extends BaseRes {
    RespBody: AA1001Resp;
}
export interface AA1001Resp {
    orgId: string;
}
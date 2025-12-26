import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA1005: 查詢TOrgDetail
 * 在TSMP Admin中查詢TSMP後台組織資料。需同時需要ORG_ID與ORG_Name才能查詢TOrg Detail。
 */
export interface ReqAA1005 extends BaseReq {
    ReqBody: AA1005Req;
}
export interface AA1005Req {
    orgId: string;
}

export interface ResAA1005 extends BaseRes {
    RespBody: AA1005Resp;
}
export interface AA1005Resp {
    orgId: string;
    orgName: string;
    createUser: string;
    createTime: string;
    contactName: string;
    parentName: string;
    orgCode: string;
    contactTel: string;
    contactMail: string;
}
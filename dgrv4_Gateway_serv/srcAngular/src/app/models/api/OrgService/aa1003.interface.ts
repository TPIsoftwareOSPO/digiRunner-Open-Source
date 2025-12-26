import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA1003: 更新TOrgByOrgID
 * 在TSMP Admin中根據ORG_ID更新該組織單位，簡稱updateTOrgByOrgId。更新成功時返回ORG_ID＆UPDATE_TIME。
 */
export interface ReqAA1003 extends BaseReq {
    ReqBody: AA1003Req;
}
export interface AA1003Req {
    parentId: string;
    newParentId: string;
    orgId: string;
    orgName: string;
    newOrgName: string;
    orgCode: string;
    newOrgCode: string;
    contactName: string;
    newContactName: string;
    contactTel: string;
    newContactTel: string;
    contactMail: string;
    newContactMail: string;
}

export interface ResAA1003 extends BaseRes {
    Res_1003: Res_1003;
}
export interface Res_1003 {
    ORG_ID: string;
    UPDATE_TIME: string;
}
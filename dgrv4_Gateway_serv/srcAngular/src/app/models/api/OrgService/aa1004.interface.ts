import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA1004: 刪除TOrg
 * 1.在TSMP Admin中刪除TSMP組織單位by ORG_ID。需同時有ORG_ID與ORG_Name才能刪除TOrg。此API只有Administrator(具有Admin Role)可以使用。
 * 2. 刪除組織單位by ORG_ID前查詢TSMP_API_MODULE, TSMPN_API_MODULE, TSMP_API任一TABLE如有符合欲刪除之ORG_ID,則該筆ORG_ID不可刪除。
 */
export interface ReqAA1004 extends BaseReq {
    ReqBody: AA1004Req;
}
export interface AA1004Req {
    orgId: string;
    orgName: string;
}

export interface ResAA1004 extends BaseRes { }
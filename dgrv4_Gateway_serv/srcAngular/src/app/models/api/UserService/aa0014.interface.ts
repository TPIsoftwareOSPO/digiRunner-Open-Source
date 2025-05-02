import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0014: 刪除TRole
 * 在TSMP Admin中刪除TSMP後台管理角色。刪除時要確認該Role中沒有任何User，
 * 也就是TSMP_USER_ROLE Table中沒有該Role的資料，然後一並將TSMP_ROLE_FUNC Table中該Role的資料刪除。
 */
export interface ReqAA0014 extends BaseReq {
    ReqBody: AA0014Req;
}
export interface AA0014Req {
    roleId: string;
    roleName: string;
}

export interface ResAA0014 extends BaseRes { }

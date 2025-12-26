import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0013: 更新TRoleFunc
 * 在TSMP Admin中更新TSMP後台管理角色的功能權限。
 */
export interface ReqAA0013 extends BaseReq {
    ReqBody: AA0013Req;
}
export interface AA0013Req {
    roleId: string;
    roleName: string;
    newRoleAlias?: string;
    newFuncCodeList: Array<string>;
}

export interface ResAA0013 extends BaseRes { }

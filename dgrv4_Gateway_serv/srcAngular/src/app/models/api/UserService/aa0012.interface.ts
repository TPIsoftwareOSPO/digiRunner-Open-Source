import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0012: 查詢TRoleList
 * 在TSMP Admin中查詢TSMP後台管理所有角色。
 */
export interface ReqAA0012 extends BaseReq { }

export interface ResAA0012 extends BaseRes {
    RespBody: AA0012Resp;
}
export interface AA0012Resp {
    funcCodeList: Array<string>;
}
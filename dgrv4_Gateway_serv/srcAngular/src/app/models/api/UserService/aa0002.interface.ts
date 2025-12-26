import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0002: 查詢TUserList
 * 在TSMP Admin中查詢TSMP後台管理使用者清單。預設查詢所有TUser。
 */
export interface ReqAA0002 extends BaseReq { }

export interface ResAA0002 extends BaseRes {
    RespBody: AA0002Resp;
}
export interface AA0002Resp {
    userID: string;
    userAlias: string;
    idTokenJwtstr?: string;
    firstTimeLogin?: boolean;
}

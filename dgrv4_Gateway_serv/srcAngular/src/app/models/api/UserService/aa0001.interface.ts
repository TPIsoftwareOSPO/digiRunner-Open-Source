import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0001: 新增TUser
 * 在TSMP Admin中新增TSMP後台管理使用者，簡稱TUser。
 * 新增成功時返回userID。
 */
export interface ReqAA0001 extends BaseReq {
    ReqBody: AA0001Req;
}
export interface AA0001Req {
    userName: string;
    userAlias: string;
    userBlock: string;
    userMail: string;
    roleIDList: Array<string>;
    orgID: string;
    encodeStatus: string; // ITEM_NO='ENABLE_FLAG' 
}

export interface ResAA0001 extends BaseRes {
    RespBody: AA0001Resp;
}
export interface AA0001Resp {
    userID: string;
}


export interface ResAA0001Before extends BaseRes {
    RespBody: ResAA0001RespBefore;
}
export interface ResAA0001RespBefore {
    constraints: Array<ValidatorFormat>;
}

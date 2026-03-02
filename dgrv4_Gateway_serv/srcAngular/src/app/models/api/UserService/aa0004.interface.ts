import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0004: 更新TUser
 * 在TSMP Admin中更新TSMP後台管理使用者資料。需同時有ID與Name才能更新TUser。
 * 此API只有Administrator可以使用，一般TUser不行。
 */
export interface ReqAA0004 extends BaseReq {
    ReqBody: AA0004Req;
}
export interface AA0004Req {
    userID: string;
    resetPwdFailTimes: boolean;
    resetBlock: boolean;
    userName: string;
    newUserName: string;
    userMail: string;
    newUserMail: string;
    userAlias: string;
    newUserAlias: string;
    status: string;
    newStatus: string;
    roleIDList: Array<string>;
    newRoleIDList: Array<string>;
    orgID: string;
    newOrgID: string;
}

export interface ResAA0004 extends BaseRes { }

export interface ResAA0004Before extends BaseRes {
    RespBody: ResAA0004RespBefore;
}
export interface ResAA0004RespBefore {
    constraints: Array<ValidatorFormat>;
}



import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0006: 更新TUserData
 * 在TSMP Admin中更新TSMP後台管理使用者資料。需同時有ID與Name，舊密碼相符時才能更新TUser密碼。
 */
export interface ReqAA0006 extends BaseReq {
    ReqBody: AA0006Req;
}
export interface AA0006Req {
    userId: string;
    userName: string;
    newUserName?: string;
    userAlias: string;
    newUserAlias?: string;
    userMail: string;
    newUserMail?: string;
    userBlock?: string;
    newUserBlock?: string;
    [key:string]:string|undefined;
}

export interface ResAA0006 extends BaseRes { }

export interface ResAA0006Before extends BaseRes {
    RespBody: ResAA0006RespBefore;
}
export interface ResAA0006RespBefore {
    constraints: Array<ValidatorFormat>;
}



import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0003: 查詢TUserDetail
 * 在TSMP Admin中查詢TSMP後台管理使用者資料。需同時有ID與Name才能查詢Tuser Detail。
 */
export interface ReqAA0003 extends BaseReq {
    ReqBody: AA0003Req;
}
export interface AA0003Req {
    userID: string;
    userName: string;
}

export interface ResAA0003 extends BaseRes {
    RespBody: AA0003Resp;
}
export interface AA0003Resp {
    userID: string;
    userName: string;
    userAlias: string;
    orgId: string;
    orgName: string;
    roleID: Array<string>;
    roleAlias: Array<string>;
    userMail: string;
    logonDate: string;
    createDate: string;
    status: string;
    statusName: string;
    pwdFailTimes: number;
    idPType?:string;
    idTokenJwtstr?:string;
}


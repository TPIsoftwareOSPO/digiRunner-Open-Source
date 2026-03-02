import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0005: 刪除TUser
 * 在TSMP Admin中刪除TSMP後台管理使用者。需同時有ID與Name才能刪除TUser。
 * 此API只有Administrator可以使用，一般TUser不行。
 */
export interface ReqAA0005 extends BaseReq {
    ReqBody: AA0005Req;
}
export interface AA0005Req {
    userID: string;
    userName: string;
}

export interface ResAA0005 extends BaseRes { }
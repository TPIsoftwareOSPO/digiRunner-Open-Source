import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0205: 刪除Client
 * 在TSMP中刪除Client資料及相關的Group, Host, Security資料。
 */
export interface ReqAA0205 extends BaseReq {
    ReqBody: AA0205Req;
}
export interface AA0205Req {
    clientID: string;
}

export interface ResAA0205 extends BaseRes { }

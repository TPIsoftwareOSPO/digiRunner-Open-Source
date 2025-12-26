import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0206: Client事件Log
 * TSMP Client可依需要將事件記錄在TSMP的資料庫中，這些事件必須事先預訂好。
 */
export interface ReqAA0206 extends BaseReq {
    ReqBody: AA0206Req;
}
export interface AA0206Req {
    isLogin: string;
    agent: string;
    eventType: string;
    eventMsg: string;
}

export interface ResAA0206 extends BaseRes { }
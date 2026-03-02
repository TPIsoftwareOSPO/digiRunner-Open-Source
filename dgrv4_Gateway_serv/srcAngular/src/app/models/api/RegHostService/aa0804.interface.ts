import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0804 extends BaseReq {
    ReqBody: AA0804Req;
}
export interface AA0804Req {
    regHostID: string; //註冊主機序號
    regHost: string; //註冊主機名稱
}

export interface ResAA0804 extends BaseRes { }
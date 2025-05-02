import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0803 extends BaseReq {
    ReqBody: AA0803Req;
}
export interface AA0803Req {
    regHost: string; //註冊主機名稱
    enabled: string; // bcrypt ITEM_NO='ENABLE_FLAG'
    clientID: string;
    memo: string;
    regHostID: string; //註冊主機序號
    regHostStatus: string; // bcrypt ITEM_NO='HOST_STATUS'
}

export interface ResAA0803Before extends BaseRes {
    RespBody: AA0803RespBefore;
}
export interface AA0803RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0803 extends BaseRes { }

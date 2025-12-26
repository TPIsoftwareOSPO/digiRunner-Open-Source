import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0414 extends BaseReq {
    ReqBody: AA0414Req;
}

export interface AA0414Req {
    dcId: number;
    dcCode: string;
    enableFlag: string; // 0:停用 1:啟用
}

export interface ResAA0414 extends BaseRes { }

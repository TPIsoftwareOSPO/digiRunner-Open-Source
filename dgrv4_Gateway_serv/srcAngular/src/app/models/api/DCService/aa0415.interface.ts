import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0415 extends BaseReq {
    ReqBody: AA0415Req;
}
export interface AA0415Req {
    dcId: number;
    dcCode: string;
}

export interface ResAA0415 extends BaseRes { }

import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0411 extends BaseReq {
    ReqBody: AA0411Req;
}
export interface AA0411Req {
    dcCode: string;
    nodeList: Array<string>;
    dcMemo: string;
}

export interface ResAA0411Before extends BaseRes {
    RespBody: AA0411RespBefore;
}
export interface AA0411RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0411 extends BaseRes {
    RespBody: AA0411Resp;
}
export interface AA0411Resp {
    dcId: Number;
}

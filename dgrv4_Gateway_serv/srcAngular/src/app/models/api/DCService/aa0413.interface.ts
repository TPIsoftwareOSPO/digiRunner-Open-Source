import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0413 extends BaseReq {
    ReqBody: AA0413Req;
}
export interface AA0413Req {
    dcId: number;
    oriDcCode: string;
    oriNodeList?: Array<string>;
    newNodeList?: Array<string>;
    dcMemo?: string;
}

export interface ResAA0413Before extends BaseRes {
    RespBody: AA0413RespBefore;
}
export interface AA0413RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0413 extends BaseRes {
    RespBody: AA0413Resp;
}
export interface AA0413Resp {
    dcId?: number;
}
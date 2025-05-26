import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB0113 extends BaseReq {
    ReqBody: DPB0113Req;
}
export interface DPB0113Req {
    oriRoleId: string;
    oriListType: string;
    oriTxIdList: Array<string>;
    newRoleId: string;
    newListType: string;
    newTxId: string;
}

export interface ResDPB0113Before extends BaseRes {
    RespBody: DPB0113RespBefore;
}
export interface DPB0113RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface RespDPB0113 extends BaseRes {
    RespBody: DPB0113Resp;
}
export interface DPB0113Resp {
    roleTxidMapIds: Array<number>;
}
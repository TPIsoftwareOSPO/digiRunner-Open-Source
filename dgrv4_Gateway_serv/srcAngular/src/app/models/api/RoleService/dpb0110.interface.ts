import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB0110 extends BaseReq {
    ReqBody: DPB0110Req;
}
export interface DPB0110Req {
    roleId: string;
    txId: string;
    listType: string; // bcryptParam加密, ITEM_NO = 'RT_MAP_LIST_TYPE'
}

export interface ResDPB0110Before extends BaseRes {
    RespBody: DPB0110RespBefore;
}
export interface DPB0110RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface RespDPB0110 extends BaseRes {
    RespBody: DPB0110Resp;
}
export interface DPB0110Resp {
    roleTxidMapIds: Array<number>;
}
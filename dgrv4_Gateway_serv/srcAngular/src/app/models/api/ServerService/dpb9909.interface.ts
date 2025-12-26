import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9909 extends BaseReq {
    ReqBody: DPB9909Req;
}

export interface DPB9909Req {
    itemNo: string;
    isDefault?: string;
    oriSubitemNo: string;
    subitemNo: string;
    subitemNameList: Array<DPB9909Item>;
    params: Array<string>;
}

export interface RespDPB9909 extends BaseRes {
    RespBody: DPB9909Resp;
}

export interface DPB9909Resp { }

export interface DPB9909Item {
    version: number;
    locale: string;
    subitemName: string;
    params: Array<string>;
}

export interface ResDPB9909Before extends BaseRes {
    RespBody: DPB9909RespBefore;
}

export interface DPB9909RespBefore {
    constraints: Array<ValidatorFormat>;
}


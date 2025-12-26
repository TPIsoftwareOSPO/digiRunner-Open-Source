import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9902 extends BaseReq {
    ReqBody: DPB9902Req;
}

export interface DPB9902Req {
    id: string;
    value: string;
    memo?:string;
    encrptionType:string;
}

export interface RespDPB9902 extends BaseRes {
    RespBody: DPB9902Resp;
}

export interface DPB9902Resp { }

export interface ResDPB9902Before extends BaseRes {
    RespBody: DPB9902RespBefore;
}
export interface DPB9902RespBefore {
    constraints: Array<ValidatorFormat>;
}

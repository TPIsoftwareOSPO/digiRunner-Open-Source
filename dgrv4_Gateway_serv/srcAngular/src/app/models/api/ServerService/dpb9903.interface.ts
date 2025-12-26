import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9903 extends BaseReq {
    ReqBody: DPB9903Req;
}

export interface DPB9903Req {
    id: string;
    oldVal: string;
    newVal: string;
    memo?:string;
    encrptionType:string;
}

export interface RespDPB9903 extends BaseRes {
    RespBody: DPB9903Resp;
}

export interface DPB9903Resp { }

export interface ResDPB9903Before extends BaseRes {
    RespBody: DPB9903RespBefore;
}
export interface DPB9903RespBefore {
    constraints: Array<ValidatorFormat>;
}

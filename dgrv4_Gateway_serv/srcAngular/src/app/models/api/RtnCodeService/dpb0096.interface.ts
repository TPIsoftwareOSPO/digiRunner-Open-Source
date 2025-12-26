import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB0096 extends BaseReq {
    ReqBody: DPB0096Req;
}
export interface DPB0096Req {
    tsmpRtnCode: string;
    tsmpRtnMsg: string;
    tsmpRtnDesc: string;
    locale: string; // BcryptParam, ITEM_NO = "RTN_CODE_LOCALE"
}

export interface ResDPB0096Before extends BaseRes {
    RespBody: DPB0096RespBefore;
}
export interface DPB0096RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResDPB0096 extends BaseRes {
    RespBody: DPB0096Resp;
}
export interface DPB0096Resp { }
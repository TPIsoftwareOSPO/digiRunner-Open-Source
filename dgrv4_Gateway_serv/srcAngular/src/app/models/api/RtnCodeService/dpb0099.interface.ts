import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB0099 extends BaseReq {
    ReqBody: DPB0099Req;
}
export interface DPB0099Req {
    tsmpRtnCode: string;
    tsmpRtnMsg: string;
    oldMsg: string;
    tsmpRtnDesc?: string;
    locale: string; // BcryptParam, ITEM_NO = "RTN_CODE_LOCALE"
}

export interface ResDPB0099Before extends BaseRes {
    RespBody: DPB0099RespBefore;
}
export interface DPB0099RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResDPB0099 extends BaseRes {
    RespBody: DPB0099Resp;
}
export interface DPB0099Resp { }
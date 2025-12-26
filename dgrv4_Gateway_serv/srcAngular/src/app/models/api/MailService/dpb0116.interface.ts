import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0116 extends BaseReq {
    ReqBody: DPB0116Req;
}
export interface DPB0116Req {
    id?: number;
    startDate: string; // yyyy/MM/dd
    endDate: string; // yyyy/MM/dd
    result: string; // bcrypt加密，ITEM_NO = 'RESULT_FLAG'
    keyword: string;
}

export interface ResDPB0116Before extends BaseRes {
    RespBody: DPB0116RespBefore;
}
export interface DPB0116RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResDPB0116 extends BaseRes {
    RespBody: DPB0116Resp;
}
export interface DPB0116Resp {
    dataList: Array<DPB0116Data>;
}
export interface DPB0116Data {
    maillogId: number;
    recipients: string; // 收件者
    subject: string; // 主旨
    createDate: string;
    result: string;
}
export interface DPB0116Trunc {
    val: string;
    t: boolean;
    ori?: string;
}

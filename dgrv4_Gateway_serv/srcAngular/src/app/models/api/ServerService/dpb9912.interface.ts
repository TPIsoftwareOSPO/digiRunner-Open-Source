import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9912 extends BaseReq {
    ReqBody: DPB9912Req;
}

export interface DPB9912Req {
    settingNo: string;
    settingName: string;
    subsettingNo: string;
    subsettingName: string;
    sortBy: number;
    isDefault: string;
    param1: string;
    param2: string;
    param3: string;
    param4: string;
    param5: string;
}

export interface RespDPB9912 extends BaseRes {
    RespBody: DPB9912Resp;
}

export interface DPB9912Resp { }

export interface ResDPB9912Before extends BaseRes {
    RespBody: DPB9912RespBefore;
}
export interface DPB9912RespBefore {
    constraints: Array<ValidatorFormat>;
}
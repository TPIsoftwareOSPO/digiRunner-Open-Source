import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9913 extends BaseReq {
    ReqBody: DPB9913Req;
}

export interface DPB9913Req {
    oriSettingNo: string;
    settingName: string;
    oriSubsettingNo: string;
    subsettingName: string;
    sortBy: number;
    isDefault: string;
    param1: string;
    param2: string;
    param3: string;
    param4: string;
    param5: string;
    version: number;
}

export interface RespDPB9913 extends BaseRes {
    RespBody: DPB9913Resp;
}

export interface DPB9913Resp { }

export interface ResDPB9913Before extends BaseRes {
    RespBody: DPB9913RespBefore;
}
export interface DPB9913RespBefore {
    constraints: Array<ValidatorFormat>;
}
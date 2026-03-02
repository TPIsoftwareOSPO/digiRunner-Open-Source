import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9911 extends BaseReq {
    ReqBody: DPB9911Req;
}

export interface DPB9911Req {
    settingNo: string,
    subsettingNo: string 
}

export interface RespDPB9911 extends BaseRes {
    RespBody: DPB9911Resp;
}

export interface DPB9911Resp {
    cusSettingId: number,
    settingNo: string,
    settingName: string,
    subsettingNo: string,
    subsettingName: string,
    sortBy: number,
    isDefault: string,
    param1: string,
    param2: string,
    param3: string,
    param4: string,
    param5: string,
    version: number,
}
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0001 extends BaseReq {
    ReqBody: CUS0001Req;
}
export interface CUS0001Req {
    settingNo: string;
    subsettingNo?: string;
    isDefault:string;
}

export interface ResCUS0001 extends BaseRes {
    RespBody: CUS0001Resp;
}
export interface CUS0001Resp {
    cus0001Items: CUS0001Item;
}
export interface CUS0001Item {
    cusSettingId: string;
    settingNo: string;
    settingName: string;
    subsettingNo: string;
    subsettingName: string;
    sortBy?: number;
    isDefault?: string;
    param1?: string;
    param2?: string;
    param3?: string;
    param4?: string;
    param5?: string;
}
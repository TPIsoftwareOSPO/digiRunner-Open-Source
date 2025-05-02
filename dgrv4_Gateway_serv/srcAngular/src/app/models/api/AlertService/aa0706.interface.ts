import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0706 extends BaseReq {
    ReqBody: AA0706Req;
}
export interface AA0706Req {
    lastAlertId?: string;
    alertEnabled: string; // ITEM_NO = 'ENABLE_FLAG'，1: 全部、2: 啟用、3: 停用
    roleName?: string;
    keyword?: string;
}

export interface ResAA0706 extends BaseRes {
    RespBody: AA0706Resp;
}
export interface AA0706Resp {
    alertSettingList: Array<AA0706AlertSetting>;
}
export interface AA0706AlertSetting {
    alertEnabled: string;
    alertID: string;
    alertName: string;
    alertSys: string;
    alertType: String;
}
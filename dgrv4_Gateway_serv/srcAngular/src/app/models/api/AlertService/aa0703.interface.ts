import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0703 extends BaseReq {
    ReqBody: AA0703Req
}
export interface AA0703Req {
    alertId: string;
    alertName: string;
}

export interface ResAA0703 extends BaseRes {
    RespBody: AA0703Resp;
}
export interface AA0703Resp {
    alertId: string; //告警編號
    alertDesc: string; //告警說明
    alertEnabled: string; //告警啟用
    alertEnabledName: string; //告警啟用
    alertInterval: number; //告警間隔(sec)
    alertMsg: string; //告警訊息
    alertName: string; //告警名稱
    alertSys: string; //告警系統
    alertType: string; //告警類型
    cFlag: string; //是否客製發送
    duration: number; //問題持續時間(sec)
    exDays: string; //例外日期
    exTime: string; //例外時間
    exType: string; //例外時間種類
    imFlag: string; //發送通訊群組
    imId: string; //通訊群組ID
    imType: string; //通訊種類
    roleInfoList: Array<AA0703RoleInfo>;
    searchMapString: string; // searchMap
    threshold: number; //門檻(次數或%)
    // keyword: string; // 前端顯示用
    // txid: string; // 前端顯示用
    // moduleName: string; // 前端顯示用
    // elapse: number; // 前端顯示用
}
export interface AA0703RoleInfo {
    roleId: string;
    roleName: string;
}
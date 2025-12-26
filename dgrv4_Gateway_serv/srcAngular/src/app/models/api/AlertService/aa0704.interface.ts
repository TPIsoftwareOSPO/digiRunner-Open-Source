import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0704 extends BaseReq {
    ReqBody: AA0704Req
}
export interface AA0704Req {
    alertId: string;
    alertName: string; //告警名稱
    alertType: string; //告警類型
    alertEnabled: string; //告警啟用
    alertSys?: string; //告警系統
    alertMsg: string; //告警訊息
    threshold: number; //門檻(次數或%)
    duration: number; //問題持續時間(sec)
    alertInterval: number; //告警間隔(sec)
    cFlag: string; //是否客製發送
    imFlag?: string; //發送通訊群組
    imType?: string; //通訊種類
    imId?: string; //通訊群組ID
    exType?: string; //例外時間種類
    exDays?: string; //例外日期
    exTime?: string; //例外時間
    alertDesc?: string; //告警說明
    roleIDList: Array<string>; //告警角色
    searchMapString?: string; // searchMap
}

export interface ResAA0704Before extends BaseRes {
    RespBody: AA0704RespBefore;
}
export interface AA0704RespBefore {
    constraints: Array<ValidatorFormat>;
}
export interface ResAA0704 extends BaseRes { }

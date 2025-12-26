import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0420 extends BaseReq {
    ReqBody: AA0420Req;
}
export interface AA0420Req {
    id?: number;
    moduleName: string;
    dcId: number;
    keyword?: string;
}

export interface ResAA0420 extends BaseRes {
    RespBody: AA0420Resp;
}
export interface AA0420Resp {
    moduleName: string;
    dcId: number;
    dataList: Array<AA0420Item>;
}
export interface AA0420Item {
    id: number;
    active: string; // "Y": 綠燈; "N": 紅燈
    moduleVersion: string;
    dcCodeList?: Array<string>; // 前端將 List 以換行符號分割，一筆資料呈現一列
    uploadTime: string;
    isBindVIsible: string; // 是否顯示[綁定]按鈕
    isUnbindVisible: string; // 是否顯示[解除綁定]按鈕
}
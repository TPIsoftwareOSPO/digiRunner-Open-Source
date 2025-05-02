import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0501 extends BaseReq {
    ReqBody: AA0501Req;
}
export interface AA0501Req {
    pastHours: number; // 前、後端預設值皆為「1」
}

export interface ResAA0501 extends BaseRes {
    RespBody: AA0501Resp;
}
export interface AA0501Resp {
    dataList: Array<AA0501Item>;
}
export interface AA0501Item {
    nodeId: string;
    startTime: string; // yyyy-MM-dd HH:mm:ss
    updateTime: string; // yyyy-MM-dd HH:mm:ss
    health: string; // 健康燈號 "Green", "Yellow", "Red"、ITEM_NO = 'NODE_HEALTH'
    healthName: string;
    node?: string;
    nodeType: string;
}

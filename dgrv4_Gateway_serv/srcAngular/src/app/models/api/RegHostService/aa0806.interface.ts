import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0806 extends BaseReq {
    ReqBody: AA0806Req
}
export interface AA0806Req {
    enableHeartbeat?: string; // bcrypt ENABLE_FLAG
    hostStatus?: string; // bcrypt HOST_STATUS
    lastReghostId?: string;
    keyword?: string;
    paging?: string; // 若為false，不用做分頁功能，「API註冊」功能需要列出全部的資料，所以paging = false、「註冊主機查詢」功能需要分頁，所以paging = true
}

export interface ResAA0806 extends BaseRes {
    RespBody: AA0806Resp;
}
export interface AA0806Resp {
    hostInfoList: Array<AA0806HostInfo>;
}
export interface AA0806HostInfo {
    clientID: string;
    enabled: string;
    enabledName: string;
    memo: string;
    regHost: string;
    regHostID: string;
    regHostStatus: string;
    regHostStatusName: string;
    heartbeatTime: string;
    bulb:string; // green: 與主機時間相差1分鐘之內(活著)、red: 與主機時間相差1分鐘超過(死掉)
}
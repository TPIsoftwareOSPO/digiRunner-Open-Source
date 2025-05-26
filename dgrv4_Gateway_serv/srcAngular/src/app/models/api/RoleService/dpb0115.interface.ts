import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0115 extends BaseReq {
    ReqBody: DPB0115Req;
}
export interface DPB0115Req {
    userName?: string; // token未傳入userName，roleList不可為空
    roleList?: Array<string>; // 有傳入時，可以不傳入userName
    txIdList?: Array<string>; // 傳入API的txID
}

export interface RespDPB0115 extends BaseRes {
    RespBody: DPB0115Resp;
}
export interface DPB0115Resp {
    dataList: Array<DPB0115Item>;
}
export interface DPB0115Item {
    txId: string;
    available: boolean;
}
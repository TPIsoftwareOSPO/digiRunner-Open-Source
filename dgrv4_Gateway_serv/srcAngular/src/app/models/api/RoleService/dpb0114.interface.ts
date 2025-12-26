import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0114 extends BaseReq {
    ReqBody: DPB0114Req;
}
export interface DPB0114Req {
    roleId: string;
    listType: string;
}

export interface RespDPB0114 extends BaseRes {
    RespBody: DPB0114Resp;
}
export interface DPB0114Resp {
    oriRoleId: string;
    oriListType: string;
    oriTxIdList: Array<string>;
}
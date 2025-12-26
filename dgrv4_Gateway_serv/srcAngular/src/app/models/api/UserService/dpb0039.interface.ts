import { BaseReq, BaseRes } from "../base.interface";

export interface ReqDPB0039 extends BaseReq {
    ReqBody: DPB0039Req;
}
export interface DPB0039Req {
    userId: string;
    keyword?: string;
}

export interface RespDPB0039 extends BaseRes {
    RespBody: DPB00039Resp;
}
export interface DPB00039Resp {
    userList: Array<DPB0039TsmpUser>
}
export interface DPB0039TsmpUser {
    userId: string;
    userName: string;
    userAlias: string;
    userEmail: string;
    orgId: string;
}
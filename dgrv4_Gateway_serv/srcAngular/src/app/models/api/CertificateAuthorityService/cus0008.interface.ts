import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0008 extends BaseReq {
    ReqBody: CUS0008Req;
}
export interface CUS0008Req {
    clientId: string;
    clientCertId: number;
}
export interface ResCUS0008 extends BaseRes {
    RespBody: any;
}
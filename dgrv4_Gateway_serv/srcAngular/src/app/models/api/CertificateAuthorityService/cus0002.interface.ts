import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0002 extends BaseReq {
    ReqBody: CUS0002Req;
}
export interface CUS0002Req {
    clientId: string;
    fileContent: string;
    fileName: string;
    cusCertId: string;
}

export interface ResCUS0002 extends BaseRes {
    RespBody: CUS0002Resp;
}
export interface CUS0002Resp {
    clientCertId: string;
}

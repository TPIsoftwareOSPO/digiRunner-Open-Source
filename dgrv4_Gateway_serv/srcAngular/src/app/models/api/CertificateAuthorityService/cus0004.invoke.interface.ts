import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0004 extends BaseReq {
    ReqBody: CUS0004Req;
}
export interface CUS0004Req {
    tspid: string;
}

export interface ResCUS0004 extends BaseRes {
    RespBody: CUS0004Resp;
}
export interface CUS0004Resp {
    apptJobId?: number;
    showUI?: CUS0004ShowUI;
}
export interface CUS0004ShowUI {
    cgRespBody: CUS0004CgRespBody;
}
export interface CUS0004CgRespBody {
    code: string;
    message: string;
    data?: CUS0004CgRespBodyItem[];
}
export interface CUS0004CgRespBodyItem {
    certid: string;
    applydate
    approvedate: string;
    state: string;
    caapplyid: string;
    serial: string;
    issuer: string;
    certtype: string;
    cn: string;
    email: string;
    notbefore: string;
    notafter: string;
}

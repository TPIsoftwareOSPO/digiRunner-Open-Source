import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0142 extends BaseReq {
    ReqBody: DPB0142Req;
}
export interface DPB0142Req {
    resource: string;
    subclass: String;
}

export interface RespDPB0142 extends BaseRes {
    RespBody: DPB0142Res;
}
export interface DPB0142Res {
    authCode: string;
    targetPort: string;
    targetPath: string;
    apiUid: string;
}

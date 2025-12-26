import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';
import { CUS0005CgRespBody } from './cus0005.invoke.interface';

export interface ReqCUS0005 extends BaseReq {
    ReqBody: CUS0005Req;
}
export interface CUS0005Req {
    apptJobId: number;
}

export interface ResCUS0005 extends BaseRes {
    RespBody: CUS0005Resp;
}
export interface CUS0005Resp {
    result: CUS0005Result;
}
export interface CUS0005Result {
    commLoopStatus: CusCommLoopStatus;
    showUI: CUS0004ShowUI;
}
export interface CUS0004ShowUI{
    cgRespBody: CUS0005CgRespBody;
}

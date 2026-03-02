import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';
import { CUS0005CgRespBody } from './cus0005.invoke.interface';
import { CUS0006CgRespBody } from './cus0006.invoke.interface';

export interface ReqCUS0006 extends BaseReq {
    ReqBody: CUS0006Req;
}
export interface CUS0006Req {
    apptJobId: number;
}

export interface ResCUS0006 extends BaseRes {
    RespBody: CUS0006Resp;
}
export interface CUS0006Resp {
    result: CUS0006Result;
}
export interface CUS0006Result {
    commLoopStatus: CusCommLoopStatus;
    showUI: CUS0006ShowUI;
}
export interface CUS0006ShowUI{
    cgRespBody: CUS0006CgRespBody;
}

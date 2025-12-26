import { EmApptJobStatus } from '../../common.enum';
import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';

export interface ReqCUS0004 extends BaseReq {
    ReqBody: CUS0004Req;
}
export interface CUS0004Req {
    apptJobId: number;
}

export interface ResCUS0004 extends BaseRes {
    RespBody: CUS0004Resp;
}
export interface CUS0004Resp {
    commLoopStatus: CusCommLoopStatus;
}



import { EmApptJobStatus } from '../../common.enum';
import { BaseReq, BaseRes } from '../base.interface';
import { CusCommLoopStatus } from './cus0003.loopstatus.interface';

export interface ReqCUS0006 extends BaseRes {
    ReqBody: CUS0006Req;
}
export interface CUS0006Req {
    apptJobId: number;
}

export interface ResCUS0006 extends BaseRes {
    RespBody: CUS0006Resp;
}
export interface CUS0006Resp {
    commLoopStatus: CusCommLoopStatus;
}



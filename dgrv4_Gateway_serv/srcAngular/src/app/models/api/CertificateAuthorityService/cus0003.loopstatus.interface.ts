import { EmApptJobStatus } from '../../common.enum';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqCUS0003 extends BaseRes {
    RespBody: CUS0003Req;
}
export interface CUS0003Req {
    apptJobId: number;
}

export interface ResCUS0003 extends BaseRes {
    RespBody: CUS0003Resp;
}
export interface CUS0003Resp {
    commLoopStatus: CusCommLoopStatus;
}
export interface CusCommLoopStatus {
    apptJobId: number;
    status: EmApptJobStatus;
    statusName: string;
    stackTrace: string;
}



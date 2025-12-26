import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0404: 啟動/停止Module
 * 啟動/停止TSMP部署的Module。
 */
export interface ReqAA0404 extends BaseReq {
    ReqBody: AA0404Req;
}
export interface AA0404Req {
    userName: string;
    orgId: string;
    id: number;
    moduleName: string;
    dcId: number;
    active: string;
}

export interface ResAA0404 extends BaseRes {
    taskId?: string;
}
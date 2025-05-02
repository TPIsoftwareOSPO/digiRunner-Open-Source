import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0405: 刪除Module
 * 刪除在TSMP部署的Module。只有Active狀態的Module不可被刪除。
 */
export interface ReqAA0405 extends BaseReq {
    ReqBody: AA0405Req;
}
export interface AA0405Req {
    id: number;
    moduleName: string;
}

export interface ResAA0405 extends BaseRes {
    RespBody: AA0405Resp;
}
export interface AA0405Resp {
    delCnt_detail: number;
    delCnt_module: number;
}
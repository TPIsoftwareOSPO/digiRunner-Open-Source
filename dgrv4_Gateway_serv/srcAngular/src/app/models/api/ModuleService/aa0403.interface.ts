import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0403: 查詢ModuleDetail
 * 查詢TSMP部署的Module詳細內容。
 */
export interface ReqAA0403 extends BaseReq {
    ReqBody: AA0403Req;
}
export interface AA0403Req {
    id: number;
    moduleName: string;
    dcId: number;
}

export interface ResAA0403 extends BaseRes {
    RespBody: AA0403Resp;
}
export interface AA0403Resp {
    id: number;
    moduleName: AA0403Trunc;
    moduleVersion: AA0403Trunc;
    moduleAppClass: AA0403Trunc;
    nodeTaskId?: number;
    moduleDesc?: string;
    active: string;
    statusTime?: string;
    statusUsesr?: string;
    uploadTime: string;
    uploaderName: string;
}
export interface AA0403Trunc {
    v: string;
    t: boolean;
    o?: string;
}
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0102: 更新TFunc
 * 更新TSMP Admin的功能名稱與說明。
 */
export interface ReqAA0102 extends BaseReq {
    ReqBody: AA0102Req;
}
export interface AA0102Req {
    funcCode: string;
    locale: string;
    funcName: string;
    newFuncName?: string;
    funcNameEn: string;
    newFuncNameEn?: string;
    desc: string;
    newDesc?: string;
    reportUrl?: string;
    newReportUrl?: string;
    isKibana?: boolean;
}

export interface ResAA0102 extends BaseRes { }

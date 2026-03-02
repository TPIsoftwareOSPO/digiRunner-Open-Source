import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0097 extends BaseReq {
    ReqBody: DPB0097Req;
}
export interface DPB0097Req {
    tsmpRtnCode?: string;
    locale?: string;
    keyword?: string;
}

export interface ResDPB0097 extends BaseRes {
    RespBody: DPB0097Resp;
}
export interface DPB0097Resp {
    dataList: Array<DPB0097Item>;
}
export interface DPB0097Item {
    tsmpRtnCode: string;
    isMsgTruncated: boolean; // true: 內容有被截斷，應顯示「顯示更多」，false: 內容未被截斷，不用顯示「顯示更多」
    oriTsmpRtnMsg: string;
    tsmpRtnMsg: string;
    isDescTruncated?: boolean; // true: 內容有被截斷，應顯示「顯示更多」，false: 內容未被截斷，不用顯示「顯示更多」
    oriTsmpRtnDesc?: string;
    tsmpRtnDesc?: string;
    locale: string;
}
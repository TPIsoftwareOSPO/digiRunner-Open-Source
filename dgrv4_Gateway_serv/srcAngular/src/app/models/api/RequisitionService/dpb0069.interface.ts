import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0069 extends BaseReq {
    ReqBody: DPB0069Req;
}
export interface DPB0069Req {
    reqOrdermId: number;
    chkLogId: number;
}

export interface ResDPB0069 extends BaseRes {
    RespBody: DPB0069Resp;
}
export interface DPB0069Resp {
    reqOrdermId: number;
    reqOrderNo: string;
    reqCreateDateTime: string;
    reqType: string;
    reqTypeName: string;
    reqSubtype: string;
    reqSubtypeName: string;
    effectiveDate: string;
    dataList: Array<DPB0069Items>;
}
export interface DPB0069Items {
    chkLogId: number;
    chkCreateDateTime: string;
    chkCreateUser: string;
    layer: string;
    chkLayerName: string;
    reviewStatus: string;
    reviewStatusName: string;
    reqComment?: string;
}
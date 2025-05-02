import { BaseReq, BaseRes } from '../base.interface';

export interface ReqAA0419 extends BaseReq {
    ReqBody: AA0419Req;
}
export interface AA0419Req {
    moduleName?: string;
    dcId: number;
    active?: string; // "Y"=是, "N"=否, 前端不傳入此欄位或傳入空值時，表示"全部"
    keyword?: string;
}

export interface ResAA0419 extends BaseRes {
    RespBody: AA0419Resp;
}
export interface AA0419Resp {
    dcCode: string;
    dataList: Array<AA0419Item>;
}
export interface AA0419Item {
    moduleName: string;
    moduleVersion: string;
    uploaderName?: string;
    uploadTime: string;
    verCnt: string;
    devArch: string; // 不顯示，"1"=V2架構;"2"=V3架構
}
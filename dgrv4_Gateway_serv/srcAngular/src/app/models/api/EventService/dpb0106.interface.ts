import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0106 extends BaseReq {
    ReqBody: DPB0106Req;
}
export interface DPB0106Req {
    eventId?: number;
    startDate: string;
    endDate: string;
    keyword?: string;
}

export interface ResDPB0106 extends BaseRes {
    RespBody: DPB0106Resp;
}
export interface DPB0106Resp {
    dataList: Array<DPB0106RespItem>;
}
export interface DPB0106RespItem {
    eventId: number;
    eventTypeId: string;
    eventTypeName: string;
    eventNameId: string;
    eventName: string;
    moduleName: string;
    moduleVersion: string;
    infoMsg: string;
    oriInfoMsg?: string;
    isMsgTruncated?: boolean;
    createDateTime: string;
    archiveFlag: string; // 封存 Y：true、N：false，當保留被勾選時(Y)，封存不可被編輯
    keepFlag: string; // 保留 Y：true、N：false
}
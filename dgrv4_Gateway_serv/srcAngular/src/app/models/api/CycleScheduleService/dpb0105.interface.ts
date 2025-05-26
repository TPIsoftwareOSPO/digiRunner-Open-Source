import { ValidatorFormat } from './../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';
import { DPB0101Cron } from './dpb0101.interface';

export interface ReqDPB0105 extends BaseReq {
    ReqBody: DPB0105Req;
}
export interface DPB0105Req {
    act: string; // U-更新/P-暫停/A-啟動/I-略過/S-作廢
    apptRjobId: string;
    lv: number;
    rjobName: string; // 僅執行[更新]時才需要帶入此參數
    remark?: string; // 僅執行[更新]時才需要帶入此參數
    cronJson: DPB0105Cron;
    effDateTime?: string; // yyyy/MM/dd HH:mm:ss, 僅執行[更新]時才需要帶入此參數
    invDateTime?: string; // yyyy/MM/dd HH:mm:ss, 僅執行[更新]時才需要帶入此參數
    oriDataList: Array<DPB0105Items>; // 既有的資料清單, 僅執行[更新]時才需要帶入此參數
    newDataList: Array<DPB0105Items>; // 使用者修改後的資料清單, 僅執行[更新]時才需要帶入此參數
}
export interface DPB0105Cron extends DPB0101Cron { }
export interface DPB0105Items {
    apptRjobDId?: number; // newDataList不一定要帶入此值, oriDataList則必須帶入
    lv?: number; // newDataList不一定要帶入此值, oriDataList則必須帶入
    refItemNo: string; // bcryptParam, ITEM_NO = 'SCHED_CATE1' oriDataList不須加密
    refSubitemNo?: string; // bcryptParam, ITEM_NO = '大類別的值' oriDataList不須加密
    inParams?: string;
    identifData?: string;
    sortBy: number;
}

export interface RespDPB0105 extends BaseRes {
    RespBody: DPB0105Resp;
}
export interface DPB0105Resp {
    apptRjobId: string;
    lv: number;
    cronExpression: string;
    nextDateTime: string;
    status: string;
    statusName: string;
    apptRjobDIds: Array<object>; // [{apptRjobDId: lv}]
}


export interface RespDPB0105Before extends BaseRes {
    RespBody: DPB0105RespBefore;
}
export interface DPB0105RespBefore {
    constraints: Array<ValidatorFormat>;
}
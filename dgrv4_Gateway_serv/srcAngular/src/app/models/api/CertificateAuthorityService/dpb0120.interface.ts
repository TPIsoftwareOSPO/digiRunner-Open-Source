import { BaseReq, BaseRes } from '../base.interface';
export interface ResDPB0120 extends BaseRes {
    RespBody: DPB0120Resp;
}
export interface DPB0120Resp {
    /**是否啟用客製功能 Y / N */
    isCusEnable: string;
}
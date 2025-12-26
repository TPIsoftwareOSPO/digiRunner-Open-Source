import { APIKey } from './aa0211.interface';
import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0225: 刪除VirtulGroup
 * 在TSMP中刪除VirtulGroup與相關API資料。若該Group已有Client參考，則不可刪除。
 */
export interface ReqAA0225 extends BaseReq {
    ReqBody: AA0225Req;
}
export interface AA0225Req {
    vgroupId: string;
    vgroupID: string;
    vgroupName: string;
}


export interface ResAA0225 extends BaseRes {
}



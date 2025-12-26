import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0064 extends BaseReq { }

export interface ResDPB0064 extends BaseRes {
    RespBody: DPB0064Resp;
}
export interface DPB0064Resp {
    dataMap: { // { reviewType: { layer: Array<DPB0064Items> } }
        API_APPLICATION: {
            // 0: Array<DPB0064Items>; // 申請者自己，前端不顯示
            1: Array<DPB0064Items>;
            2: Array<DPB0064Items>;
        },
        API_ON_OFF: {
            // 0: Array<DPB0064Items>; // 申請者自己，前端不顯示
            1: Array<DPB0064Items>;
            2: Array<DPB0064Items>;
        }
    };
    typeMap: object; // { itemNo: itemName }
}
export interface DPB0064Items {
    chkLayerId: number;
    reviewType: string;
    layer: string;
    roleId: string;
    status: string;
    lv: number;
    roleName: string;
}
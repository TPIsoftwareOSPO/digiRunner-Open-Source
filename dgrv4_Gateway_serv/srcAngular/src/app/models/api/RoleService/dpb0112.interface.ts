import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0112 extends BaseReq {
    ReqBody: DPB0112Req;
}
export interface DPB0112Req {
    roleId: string;
    listType: string; // bcryptParam加密, ITEM_NO = 'RT_MAP_LIST_TYPE'
}

export interface RespDPB0112 extends BaseRes {
    RespBody: DPB0112Resp;
}
export interface DPB0112Resp {
    oriRoleId: string;
    oriRoleAlias: string;
    oriRoleName: string;
    oriListType: string;
    oriTxIdList: Array<string>;
    oriTxIdString: string;
}
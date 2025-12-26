import { BaseReq, BaseRes } from '../base.interface';

/**
 * AA0009: 登出TUser
 * 在TSMP Admin中將TUser登出，即讓Token失效。預計做法: 執行登出時，系統在LOGOFF_DATE記錄時間，
 * 則接下來所有進來請求的AccessToken或RefreshToke的timestamp皆要大於此時間才行，否則回覆"0001"(請重新登入)。
 */
export interface ReqAA0009 extends BaseReq { }

export interface ResAA0009 extends BaseRes { }

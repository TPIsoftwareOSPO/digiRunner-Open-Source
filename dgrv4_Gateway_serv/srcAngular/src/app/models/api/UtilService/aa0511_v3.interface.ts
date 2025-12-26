import { BaseReq, BaseRes } from '../base.interface';
/**
 * AA0511: getAuthCode
 * 外部Client(ex. Composer)取得授權碼AuthCode，即可由此授權碼向TSMP取得AccessToken。
 * *AuthCode中會有user, client, expired等資訊
 */
export interface ReqAA0511_v3 extends BaseReq {
    ReqBody: AA0511Req;
}
export interface AA0511Req {
    authType: string;
    resource?:string;
    subclass?:string;
}
export interface ResAA0511_v3 extends BaseRes {
    RespBody: AA0511Res;

}
export interface AA0511Res {
    authCode: string;
    targetPort?: string;
    targetPath?: string;
}




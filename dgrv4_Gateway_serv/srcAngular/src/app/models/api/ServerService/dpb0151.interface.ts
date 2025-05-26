import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0151 extends BaseReq {
    ReqBody: DPB0151Req;
}
export interface DPB0151Req {
    idpType:string;
    clientId:string;
    clientMima:string;
    clientName:string;
    clientStatus:string;
    idpWellKnownUrl:string;
    callbackUrl:string;
    authUrl?:String;
    accessTokenUrl?:string;
    scope?:string;
}

export interface RespDPB0151 extends BaseRes {
    RespBody: DPB0151Res;
}
export interface DPB0151Res {

   
 }
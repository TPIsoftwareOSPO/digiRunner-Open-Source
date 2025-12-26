import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB0149 extends BaseReq {
    ReqBody: DPB0149Req;
}
export interface DPB0149Req {
   
}

export interface RespDPB0149 extends BaseRes {
    RespBody: DPB0149Res;
}
export interface DPB0149Res {

    dgrAcIdpInfo: Array<DPB0149Item>;
  
 }

 export interface DPB0149Item{
    idpType:string;
    clientId:string;
    clientMima:string;
    clientName:string;
    clientStatus:string;
    idpWellKnownUrl:string;
    callbackUrl:string;
    authUrl:String;
    accessTokenUrl:string;
    scope:string;
    createDateTime:string;
    createUser:string;
    updateDateTime?:string;
    updateUser?:string;
    id:string
 }
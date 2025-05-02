import { BaseReq, BaseRes } from '../base.interface';


export interface ReqDPB0152 extends BaseReq {
    ReqBody: DPB0152Req;
}
export interface DPB0152Req {
   id:string;
}

export interface RespDPB0152 extends BaseRes {
    RespBody: DPB0152Res;
}
export interface DPB0152Res {

   
 }
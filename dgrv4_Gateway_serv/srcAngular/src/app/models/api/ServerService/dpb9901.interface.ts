import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9901 extends BaseReq {
    ReqBody: DPB9901Req;
}

export interface DPB9901Req {
    id: string;    
}

export interface RespDPB9901 extends BaseRes {
    RespBody: DPB9901Resp;
}

export interface DPB9901Resp {
    id:string;
    value:string;
    memo?:string;
 }
import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9918 extends BaseReq {
    ReqBody: DPB9918Req;
}

export interface DPB9918Req {
    fileId: number;
    refFileCateCode: string;    
    refId: number;
    fileName:string;
    isBlob:string;
    tmpFileName:string;
    version:number;
}

export interface RespDPB9918 extends BaseRes {
    RespBody: DPB9918Resp;
}

export interface DPB9918Resp { }

export interface ResDPB9918Before extends BaseRes {
    RespBody: DPB9918RespBefore;
}
export interface DPB9918RespBefore {
    constraints: Array<ValidatorFormat>;
}
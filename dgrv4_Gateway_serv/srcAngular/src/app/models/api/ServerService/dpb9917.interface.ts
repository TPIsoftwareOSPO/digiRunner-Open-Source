
import { BaseReq, BaseRes } from '../base.interface';
import { ValidatorFormat } from '../../validator.interface';

export interface ReqDPB9917 extends BaseReq {
    ReqBody: DPB9917Req;
}

export interface DPB9917Req {
    refFileCateCode: string;    
    refId:number;
    isTmpfile:string;
    fileName:string;
    tmpfileName:string;
}

export interface ResDPB9917Before extends BaseRes {
    RespBody: DPB9917RespBefore;
}
export interface DPB9917RespBefore {
    constraints: Array<ValidatorFormat>;
}
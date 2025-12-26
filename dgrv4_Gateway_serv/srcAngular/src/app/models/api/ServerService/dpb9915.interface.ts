import { ValidatorFormat } from '../../validator.interface';
import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9915 extends BaseReq {
    ReqBody: DPB9915Req;
}

export interface DPB9915Req {
    fileId: number;    
    startDate: string;
    endDate: string;
    refFileCateCode: string;
    refId: number;
    keyword: string;
    isTmpfile: string;
}

export interface RespDPB9915 extends BaseRes {
    RespBody: DPB9915Resp;
}

export interface DPB9915Resp { 
    autoDeleteDays:string;
    dataList:Array<DPB9915Item>;
}

export interface DPB9915Item {
    fileId:number;
    fileName:DPB9915Trunc;
    refFileCateCode:string;
    refId:number;
    lastUpdDateTime:string;
    lastUpdUser:string;
    filePath:string;
    version:number;
}

export interface DPB9915Trunc {
    val: string; // 顯示用
    t: boolean; // 是否被截斷
    ori?: string; // 原始值，被截斷才回傳
}

export interface ResDPB9915Before extends BaseRes {
    RespBody: DPB9915RespBefore;
}
export interface DPB9915RespBefore {
    constraints: Array<ValidatorFormat>;
}

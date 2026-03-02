import { BaseRes } from "../base.interface";

export interface ResDPB0121 extends BaseRes {
    RespBody:DPB0121Resp;
}
export interface DPB0121Resp{
    refreshMemListUrls: Array<string>;
    redoUrls:Array<string>;
}
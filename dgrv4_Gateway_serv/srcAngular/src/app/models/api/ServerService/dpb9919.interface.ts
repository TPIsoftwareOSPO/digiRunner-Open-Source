import { BaseReq, BaseRes } from '../base.interface';

export interface ReqDPB9919 extends BaseReq {
    ReqBody: DPB9919Req;
}

export interface DPB9919Req {
    fileList: DPB9919Item[];    
    newIsTmpfile:string;
}

export interface DPB9919Item{
    fileId:number;
    oriIsTmpFile:string;
}
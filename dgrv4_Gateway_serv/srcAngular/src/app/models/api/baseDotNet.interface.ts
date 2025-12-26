import { ReqHeader, ResHeader } from './base.header.interface';
export interface BaseDotNetReq {
    ReqHeader: ReqHeader;
}
export interface BaseDotNetRes {
    resHeader: ResHeader
}
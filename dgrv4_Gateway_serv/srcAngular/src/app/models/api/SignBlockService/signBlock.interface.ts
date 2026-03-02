import { BaseRes } from '../base.interface';

export interface ResGetSignBlock extends BaseRes {
    Res_getSignBlock: Res_getSignBlock
}
export interface Res_getSignBlock {
    signBlock: string;
}
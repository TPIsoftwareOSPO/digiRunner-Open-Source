import { FormOperate } from './common.enum';


export interface FormParams {
    operate?: FormOperate;
    data?: any;
    displayInDialog?: boolean;
    afterCloseCallback ?:Function;
  }

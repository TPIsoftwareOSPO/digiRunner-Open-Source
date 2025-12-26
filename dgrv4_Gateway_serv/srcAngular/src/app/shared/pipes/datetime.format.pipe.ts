import { Pipe, PipeTransform } from '@angular/core';
import * as dayjs from 'dayjs';


@Pipe({
    name: 'datetime_format'
})
export class DatetimeFormatPipe implements PipeTransform {

    transform(input: any,arg1 : any): any {
     
        if (!input) return;
        if (input.length != 20) return;
        let position = 0
        let YYYY = input.substr(0, 4);
        let MM = input.substr(4, 2);
        let DD = input.substr(6, 2);
        let HH = input.substr(9, 2);
        let mm = input.substr(11, 2);
        let ss = input.substr(13, 2);
        let utc = input.slice(15);
        utc = utc.slice(0, 3) + ':' + utc.slice(3);
        let newDate = dayjs(`${YYYY}-${MM}-${DD} ${HH}:${mm}:${ss}`).toDate();
        if (arg1 === 'time_ignore') 
            return dayjs(newDate).format('YYYY-MM-DD');
        else
            return dayjs(newDate).format('YYYY-MM-DD HH:mm:ss');
    }
}
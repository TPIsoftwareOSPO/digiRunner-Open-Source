import { Pipe, PipeTransform } from '@angular/core';
import * as dayjs from 'dayjs';

@Pipe({
    name: 'utc_datetime_format'
})
export class UTCDatetimeFormatPipe implements PipeTransform {
    transform(input: any): any {
        if (!input) return;
        return dayjs(input).format(`YYYY-MM-DD HH:mm:ss.SSS`);
    }
}
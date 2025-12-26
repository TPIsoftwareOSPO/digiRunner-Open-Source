import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'event_flag'
})
export class EventFlagPipe implements PipeTransform {
    transform(input: any, separator: string = ' ', limit?: number): any {
        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case 'N':
                return 'assets/images/star_border_white_20dp.png';
            case 'Y':
                return 'assets/images/star_googyellow500_20dp.png';
        }
    }
}
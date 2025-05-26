import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'api_status'
})
export class APIStatusPipe implements PipeTransform {
    transform(input: any, separator: string = ' ', limit?: number): any {
        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '0':
                return 'assets/images/error.png';
            case '1':
                return 'assets/images/icon_green.png';
            case '2':
                return 'assets/images/icon_red.png';
        }
    }
}

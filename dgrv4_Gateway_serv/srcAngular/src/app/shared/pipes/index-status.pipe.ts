import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'api_status'
})
export class IndexStatusPipe implements PipeTransform {
    transform(input: string): string {
        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case 'green':
                return 'assets/images/icon_green.png';
            case 'red':
                return 'assets/images/icon_red.png';
            case 'yellow':
                return 'assets/images/icon_yellow.png';
            default:
                return '';
        }
    }
}

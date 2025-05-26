import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'apisrc'
})
export class APISrcPipe implements PipeTransform {

    transform(input: any, separator: string = ' ', limit?: number): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case 'M':
                return 'Java Module';
            case 'N':
                return 'Net Module';
            case 'R':
                return 'Registered';
            case 'C':
                return 'Composed';
        }

    }
}
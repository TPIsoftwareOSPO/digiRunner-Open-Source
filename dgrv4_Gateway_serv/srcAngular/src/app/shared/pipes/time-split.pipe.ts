import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'time_split'
})
export class TimeSplitPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        };
        return input.substr(0, 2) + ':' + input.substr(2);
        
    }
}
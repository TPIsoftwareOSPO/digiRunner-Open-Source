import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'string_length'
})
export class StringLengthPipe implements PipeTransform {
    transform(str: string, length: number): string {
        if (!str) return '';
        if (str.length > length) {
            return str.substring(0, length) + '...';
        }
        else {
            return str;
        }
    }
}

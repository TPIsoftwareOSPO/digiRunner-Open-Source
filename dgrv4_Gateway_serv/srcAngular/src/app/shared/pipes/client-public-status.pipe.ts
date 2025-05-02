import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'client_public_status'
})
export class ClientPublicStatusPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '2':
                return '對內';
            case '1':
                return '對外';
            case '0':
                return '對內及對外';
        }

    }
}
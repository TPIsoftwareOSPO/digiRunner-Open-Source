import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'client_status'
})
export class ClientStatusPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '3':
                return 'Locked';
            case '2':
                return 'Inactive';
            case '1':
                return 'Active';
        }

    }
}
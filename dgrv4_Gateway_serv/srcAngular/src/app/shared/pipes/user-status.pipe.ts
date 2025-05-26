import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'user_status'
})
export class UserStatusPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '1':
                return 'Active';
            case '2':
                return 'Disabled';
            case '3':
                return 'Locked';
        }
        
    }
}
import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'task_status'
})
export class TaskStatusPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '1':
                return 'Successful';
            case '0':
                return 'Fail';
        }
        
    }
}
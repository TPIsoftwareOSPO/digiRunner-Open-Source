import { Pipe, PipeTransform } from '@angular/core';



@Pipe({
    name: 'module_status'
})
export class ModuleStatusPipe implements PipeTransform {

    transform(input: any): any {

        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '0':
                return 'assets/images/icon_acr.png';
            case '1':
                return 'assets/images/icon_acg.png';
        }

    }
}

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'api_jwt_status'
})
export class APIJWTSStatusPipe implements PipeTransform {
    transform(input: any): any {
        if (Object.prototype.toString.call(input) != '[object String]') {
            return input;
        }
        switch (input) {
            case '0':
                return '不使用';
            case '1': // JWE
                return 'JWE';
            case '2': // JWS
                return 'JWS';
            case null:
                return '不使用';
        }
    }
}
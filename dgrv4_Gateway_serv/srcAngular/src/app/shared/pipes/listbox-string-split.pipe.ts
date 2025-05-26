import { Pipe, PipeTransform  } from '@angular/core';



@Pipe({
  name: 'listbox_string_split'
})
export class ListboxStringSplitPipe implements PipeTransform {

  transform (input: any, separator: string = ' ', limit?: number): any {

    if (Object.prototype.toString.call(input) != '[object String]') {
      return input;
    }

    return input.split(separator, limit).map((val:string) => {
        return {label : val,value : val}
    });
  }
}

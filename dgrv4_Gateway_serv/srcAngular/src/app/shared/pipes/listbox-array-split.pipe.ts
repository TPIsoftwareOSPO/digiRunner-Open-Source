import { Pipe, PipeTransform  } from '@angular/core';
 


@Pipe({
    name: 'listbox_array_split',
    standalone: false
})
export class ListboxArraySplitPipe implements PipeTransform {
  
  transform (input: Array<string>, limit?: number): any {
    
    if (Object.prototype.toString.call(input) != '[object Array]') {
      return input;
    }
    
    return input.map(val => {
        return {label : val,value : val}
    });
  }
}
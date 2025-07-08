import { ToolService } from 'src/app/shared/services/tool.service';
import { SidebarService } from './../../layout/components/sidebar/sidebar.service';
import { Pipe, PipeTransform  } from '@angular/core';

@Pipe({
  name: 'transform_menu'
})
export class TransformMenuNamePipe implements PipeTransform {
    constructor( private toolService:ToolService){

    }

  transform (input: string): any {

    if (Object.prototype.toString.call(input) != '[object String]') {
      return input;
    }

    const result = this.toolService.getFuncList()?.find(r => r.funcCode === input);
    return result ? result.funcName : input;
  }
}

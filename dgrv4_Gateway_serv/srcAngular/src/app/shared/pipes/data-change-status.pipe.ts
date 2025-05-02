import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable } from 'rxjs';

@Pipe({
  name: 'dataChangeStatus'
})
export class DataChangeStatusPipe implements PipeTransform {

  constructor(private translateService: TranslateService) { }

  //提供ac0230 設定檔匯出/入 表格資料替換多國使用
  transform(key: string): Observable<string> {
    return this.translateService.get(`client_related.${key}`);
  }
}

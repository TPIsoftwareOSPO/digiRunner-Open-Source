import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';

@Pipe({
  name: 'reportType',
})
export class ReportTypePipe implements PipeTransform {
  constructor(private translateService: TranslateService) {}

  // "embedded_url_tip": {
  //     "1": "三種功能類型",
  //     "1_1": "kibana報表",
  //     "1_2": "嵌入頁面",
  //     "1_3": "另開頁面",
  //  }

  //提供ac0105
  transform(key: string): Observable<string> {
    let translateKey: string = '';
    switch (key) {
      case 'SYS_RPT':
        translateKey = '1_1';
        break;
      case 'IFRAME':
        translateKey = '1_2';
        break;
      case 'LINK':
        translateKey = '1_3';
        break;
      default:
        break;
    }
    return translateKey=='' ? of(''): this.translateService.get(`embedded_url_tip.${translateKey}`);
  }
}

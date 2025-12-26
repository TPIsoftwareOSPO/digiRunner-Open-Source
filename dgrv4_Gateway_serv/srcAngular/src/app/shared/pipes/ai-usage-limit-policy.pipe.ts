import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';

@Pipe({
  name: 'aiUsageLimitPolicy',
})
export class AiUsageLimitPolicyPipe implements PipeTransform {
  constructor(private translateService: TranslateService) {}

  //提供dpb0255
  transform(key: string): Observable<string> {

    const key_format = key.toLocaleLowerCase();
    return key=='' ? of(''): this.translateService.get(`ai.${key_format}`);
  }
}

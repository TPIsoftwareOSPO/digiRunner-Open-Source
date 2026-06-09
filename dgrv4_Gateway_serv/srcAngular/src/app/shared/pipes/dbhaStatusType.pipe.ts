import { Pipe, PipeTransform } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Observable, of } from 'rxjs';

@Pipe({
    name: 'dbhaStatusType',
    standalone: true
})
export class DbhaStatusTypePipe implements PipeTransform {
  constructor(private translateService: TranslateService) {}

  transform(key: string): Observable<string> {
    return this.translateService.get(`dbha.${key}`);
  }
}

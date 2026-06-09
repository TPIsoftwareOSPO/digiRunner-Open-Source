import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css'],
    standalone: false
})
export class AppComponent {
  title = 'tsmp-frontend'; //v4
  defaultLang: string = '';
  constructor(
    private translate: TranslateService,
    private httpClient: HttpClient
  ) {
    // console.log(navigator.language);
    const locale = ['zh-tw', 'en-us','zh-cn','ja'];
    this.defaultLang = (
      navigator.language ||
      this.translate.getBrowserLang() ||
      'en-us'
    ).toLowerCase();



    if (locale.find((item) => item == this.defaultLang)) {
      this.defaultLang = this.defaultLang;
    } else {
      this.defaultLang = 'en-us';
    }
    translate.setDefaultLang(this.defaultLang);
    translate.use(this.defaultLang);
  }
}

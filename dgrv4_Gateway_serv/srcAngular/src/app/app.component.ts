import { HttpClient } from '@angular/common/http';
import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
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
    // console.log(this.defaultLang);
    translate.setDefaultLang(this.defaultLang);
    translate.use(this.defaultLang)
    translate.setDefaultLang('en-us');
    // console.log(this.translate.getBrowserLang());
    // this.httpClient.get(`assets/i18n/${navigator.language.toLowerCase()}.json`).subscribe((translations: any) => {
    //   console.log(translations);
    // this.translate.setTranslation(translations, true);
    // });
  }
}

import { environment } from './../../../../environments/environment';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-online-console',
  templateUrl: './online-console.component.html',
  styleUrls: ['./online-console.component.css']
})
export class OnlineConsoleComponent implements OnInit {


  url:SafeResourceUrl = {};

  constructor(
    private sanitizer: DomSanitizer,
  ) { }

  ngOnInit(): void {

    let tarUrl = '';
    if(location.hostname =='localhost')
    {
       tarUrl = environment.apiUrl;
    }
    else{
        tarUrl = `${location.protocol}//${location.hostname}:${location.port}`;
    }

    const url:string = `${tarUrl}/dgrv4/onlineConsole2/onlineConsole2.html`;
    this.url = this.sanitizer.bypassSecurityTrustResourceUrl(url);
  }

}

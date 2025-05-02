import { environment } from './../../../environments/environment.prod';
import { ToolService } from './tool.service';
import { NavigationExtras, Router } from '@angular/router';
import { Injectable } from "@angular/core";

@Injectable()
export class LogoutService {
  options?: NavigationExtras;
  constructor(
    private router: Router,
    private toolService: ToolService
  ) { }

  // AA0510取回 ac config時內若有logoutUrl欄位則 登出時需導引回此欄位內容 .20220630
  logout(re?: boolean) {

    if (re) {
      this.options = {
        queryParams: {
          re: Date.now(),
        }
      };
    }

    if (location.hostname == 'localhost' || location.hostname == '127.0.0.1') {
      console.log('開發模式', '登出後根據設定檔內容跳轉功能 -> *關閉*，如需測試登出跳轉請至程式碼開啟')
      sessionStorage.clear();
      if (this.router) this.router.navigate(['/login'], this.options).then(() => {
        window.location.reload();
      });



      return;
    }

    // console.log('logoutUrl: ' + this.toolService.getAcConf())
    const logoutUrl: string | undefined = this.toolService.getAcConf() ? this.toolService.getAcConf().logoutUrl : undefined;
    // console.log('logoutUrl: ' + logoutUrl)
    sessionStorage.clear();

    if (logoutUrl && logoutUrl != '') {
      window.location.href = logoutUrl;
    }
    else {
      if (this.router) this.router.navigate(['/login'],this.options).then(() => {
        window.location.reload();
      });
    }
  }
}

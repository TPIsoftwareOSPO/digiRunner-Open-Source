import { catchError, finalize } from 'rxjs/operators';
import { DialogService } from 'primeng/dynamicdialog';
import { LogoutService } from './../../../shared/services/logout.service';
import { ToolService } from './../../../shared/services/tool.service';
import { Component, OnInit } from '@angular/core';
import { Router, NavigationEnd } from '@angular/router';
import { SidebarService } from '../sidebar/sidebar.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import * as dayjs from 'dayjs';
import * as base64 from 'js-base64';
import { MenuItem } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-header',
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss'],
})
export class HeaderComponent implements OnInit {
  items: MenuItem[] = [];
  pushRightClass: string = 'push-right';
  // username: any;
  userAlias: string = '';
  acConf: AA0510Resp;

  envList: Array<{ envName: string; envUrl: string }> = [];

  aliveSec: number | undefined = 300; //從session中的expires_in更新
  minStr: string = '';
  secStr: string = '';
  timeInterval: any;

  aliveStr: string = '';

  lastTimestamp: Date = new Date();

  userImg: string = 'assets/images/user.png';
  idTokenJwtstr: string = sessionStorage.getItem('idTokenJwtstr') ?? '';

  constructor(
    public router: Router,
    private siderbar: SidebarService,
    private toolService: ToolService,
    private userService: UserService,
    private logoutService: LogoutService,
    private dialogService: DialogService,
    private translate: TranslateService
  ) {
    this.router.events.subscribe((val) => {
      if (
        val instanceof NavigationEnd &&
        window.innerWidth <= 992 &&
        this.isToggled()
      ) {
        this.toggleSidebar();
      }
    });
    this.acConf = this.toolService.getAcConf();
    if (this.acConf && this.acConf.edition) {
      const editionMap: { [key: string]: string } = {
        Express: 'Community',
        Alpha: 'Alpha',
        Enterprise: '',
        Enterprise_Lite: 'Enterprise Lite',
      };

      this.acConf.edition =
        editionMap[this.acConf.edition] ?? this.acConf.edition;
    }
    this.aliveSec = sessionStorage.getItem('expires_in')
      ? Number(sessionStorage.getItem('expires_in'))
      : undefined;
  }

  async ngOnInit() {
    this.translate.get(['profile', 'about', 'logout']).subscribe((dict) => {
      this.items = [
        {
          label: dict['profile'],
          icon: 'pi pi-fw pi-user',
          command: () => {
            this.router.navigate(['/profile']);
          },
        },
        {
          label: dict['about'],
          icon: 'pi pi-fw pi-info-circle',
          command: () => {
            this.router.navigate(['/about']);
          },
        },
        {
          label: dict['logout'],
          icon: 'pi pi-fw pi-sign-out',
          command: () => {
            this.onLoggedout();
          },
        },
      ];
    });

    this.envList = this.toolService.getEnvListData();
    if (this.envList.length > 0) {
      this.envList.forEach((item) => {
        this.items.push({
          label: item.envName,
          icon: 'pi pi-sitemap',
          command: () => {
            this.redirect(item.envUrl);
          },
        });
      });
    }
    this.userAlias = this.toolService.getUserAlias();
    // this.username = this.toolService.getUserName();
    // if (this.username.length > 3) {
    //     let substr = this.username.substr(0, 3);
    //     for (let i = 0; i = (this.username.length - substr.length); i++) {
    //         substr += '*';
    //     }
    //     this.username = substr;
    // }
    this.toolService.getExpiredTime().subscribe((date) => {
      this.aliveSec = sessionStorage.getItem('expires_in')
        ? Number(sessionStorage.getItem('expires_in'))
        : undefined;
      this.lastTimestamp = new Date();
    });

    this.toolService.procClearExpiredTimeout().subscribe((sign) => {
      if (sign) {
        this.onLoggedout();
      }
    });

    // if(location.hostname != 'localhost')
    this.expiredProcess();

    if (this.idTokenJwtstr != '') {
      const tmpTokenPool: string[] = this.idTokenJwtstr.split('.');
      // console.log(this.toolService.base64_decode(tmpTokenPool[1]))
      const strToken: string =
        tmpTokenPool.length > 1 ? base64.Base64.decode(tmpTokenPool[1]) : '';
      // const strToken:string = tmpTokenPool.length>1 ? this.toolService.Base64Decoder(tmpTokenPool[1]) : '';

      if (strToken != '') {
        const jsonToken = JSON.parse(strToken);
        // console.log(jsonToken);
        if (jsonToken) {
          if (jsonToken['picture'] && jsonToken['picture'] != '')
            this.userImg = jsonToken['picture'];
          this.userAlias = jsonToken['name']
            ? jsonToken['name']
            : jsonToken['email']
            ? jsonToken['email']
            : jsonToken['sub']
            ? jsonToken['sub']
            : this.userAlias;
        }
      }
    }
  }

  //閒置倒數歸零則登出
  expiredProcess() {
    if (!this.aliveSec) return;
    if (this.timeInterval) clearInterval(this.timeInterval);
    this.timeInterval = setInterval(() => {
      const nowTimestamp: Date = new Date();
      const diffSecs = Math.round(
        dayjs(nowTimestamp).diff(dayjs(this.lastTimestamp), 'millisecond') /
          1000
      );

      if (
        this.aliveSec &&
        (this.aliveSec == 0 ||
          diffSecs >= Number(sessionStorage.getItem('expires_in')))
      ) {
        clearInterval(this.timeInterval);
        // this.router.navigate(['/login']);
        // console.log('閒置登出');
        this.logoutProc();
        return;
      }
      this.aliveSec = Number(sessionStorage.getItem('expires_in')) - diffSecs;
      this.minStr = ('0' + Math.floor(this.aliveSec! / 60).toString()).slice(
        -2
      );
      this.secStr = ('0' + (this.aliveSec! % 60).toString()).slice(-2);

      // console.log(this.aliveSec)
      this.aliveStr = this.formatTime(this.aliveSec)
    }, 1000);
  }

  formatTime(seconds) {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    // 補零函式
    const pad = (num) => String(num).padStart(2, '0');

    if (hrs > 0) {
      return `${pad(hrs)} : ${pad(mins)} : ${pad(secs)}`;
    } else {
      return `${pad(mins)} : ${pad(secs)}`;
    }
  }

  isToggled(): boolean {
    const dom: Element = document.querySelector('body')!;
    return dom.classList.contains(this.pushRightClass);
  }

  toggleSidebar() {
    const dom: any = document.querySelector('body');
    dom.classList.toggle(this.pushRightClass);
  }

  click($event) {
    this.siderbar.clear();
  }

  rltAndLtr() {
    const dom: any = document.querySelector('body');
    dom.classList.toggle('rtl');
  }

  onLoggedout() {
    if (this.timeInterval) clearInterval(this.timeInterval);
    this.logoutProc();
  }

  logoutProc() {
    // 登出前 清除所有開窗
    this.dialogService.dialogComponentRefMap.forEach((dialog) => {
      dialog.destroy();
    });
    //
    this.userService.logoutTUser().subscribe(() => {
      this.logoutService.logout();
    });
  }

  changeLang(language: string) {
    // this.translate.use(language);
  }

  redirect(urlParam: string) {
    let urlPool = urlParam.split('/');

    // console.log(urlParam.replace('/'+urlPool[urlPool.length-1],'login?info=' + urlPool[urlPool.length-1]))
    if (window.location.href.indexOf('localhost') > -1) {
      this.router.navigateByUrl(
        '/udpssologin?info=' + urlPool[urlPool.length - 1]
      );
    } else {
      let redirectUrl = urlParam.replace(
        '/' + urlPool[urlPool.length - 1],
        'login?info=' + urlPool[urlPool.length - 1]
      );
      window.location.href = redirectUrl;
    }
  }
}

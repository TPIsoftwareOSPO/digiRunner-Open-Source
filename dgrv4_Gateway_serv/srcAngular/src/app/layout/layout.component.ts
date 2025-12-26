import { Component, HostListener, OnInit } from '@angular/core';
import { MatDrawerToggleResult, MatSidenav } from '@angular/material/sidenav';
import { AboutService } from '../shared/services/api-about.service';
import { ToolService } from '../shared/services/tool.service';
import { FirstTimeLoginComponent } from '../shared/first-time-login/first-time-login.component';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DialogService } from 'primeng/dynamicdialog';
import { MessageService } from 'primeng/api';
import { AlertService } from '../shared/services/alert.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {
  // collapedSideBar: boolean = false;

  appropriateClass: string = '';
  mode: string = 'side'; //side,over
  isMin: boolean = false;

  firstTimeLogin: boolean = sessionStorage.getItem('firstTimeLogin') == 'true';

  constructor(
    private aboutService: AboutService,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    private dialogService: DialogService,
    private messageService: MessageService,
    private alertService: AlertService,
    private router: Router,
  ) {}

  @HostListener('window:resize', ['$event'])
  getScreen() {
    //console.log(window.innerHeight);
    if (window.innerHeight <= 412) {
      this.appropriateClass = 'bottomRelative';
    } else {
      this.appropriateClass = 'bottomStick';
    }

    this.mode = window.innerWidth <= 992 ? 'over' : 'side';
    // this.minContent = window.innerWidth < 576;
  }

  ngOnInit(): void {
    this.aboutService.queryModuleVersion().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.aboutService.setModuleVersionData(res.RespBody);
        this.toolService.writeToken(
          res.RespBody.majorVersionNo,
          'majorVersionNo'
        );
      }
    });
   if(this.firstTimeLogin) this.checkFirstLogin();
  }

  async checkFirstLogin() {
    const codes = [
      'changeMimaFirstLogin',
      'message.update',
      'changeMimaFirstLogin',            
      'message.success',
      'plz_login_again',
    ];
    const dict = await this.toolService.getDict(codes);
    const ref = this.dialogService.open(FirstTimeLoginComponent, {
      // data: { details:  },
      header: dict['changeMimaFirstLogin'],
      width: '300px',
      styleClass: 'cHeader cContent cIcon',
      closable: false,
    });

    ref.onClose.subscribe((res) => {
      this.alertService.logout(
        `${dict['message.update']} ${dict['message.success']}!`,
        `${dict['plz_login_again']}!`
      );
      setTimeout(() => {
         this.router.navigate(['/login'])
      }, 3000);
    });
  }

  // receiveCollapsed($event) {
  //   this.collapedSideBar = $event;
  // }

  toggleSideNav(drawer: MatSidenav) {
    drawer.toggle().then((result) => {
      // console.log('選單狀態：' + result);
      this.isMin = !result;
    });
  }

  closeMask() {
    const dom: any = document.querySelector('body');
    dom.classList.toggle('push-right');
  }
}

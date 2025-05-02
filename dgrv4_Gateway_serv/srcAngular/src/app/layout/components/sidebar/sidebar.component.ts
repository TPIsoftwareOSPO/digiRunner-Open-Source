import { LogoutService } from './../../../shared/services/logout.service';
import { ApiBaseService } from './../../../shared/services/api-base.service';
import { AlertService } from './../../../shared/services/alert.service';
import { FuncService } from 'src/app/shared/services/api-func.service';
// import { AlertService } from 'src/app/shared/services/alert.service';
import { TokenService } from 'src/app/shared/services/api-token.service';
import { SubMenu } from 'src/app/models/menu.model';
import { OnInit } from '@angular/core';
import { Menu } from './../../../models/menu.model';
import { TransformMenuNamePipe } from './../../../shared/pipes/transform-menu-name.pipe';
import { BaseComponent } from 'src/app/layout/base-component';
import { Component, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { Router, NavigationEnd, RouteConfigLoadEnd, CanActivate, ActivatedRouteSnapshot } from '@angular/router';
import { ActivatedRoute } from '@angular/router'
import { SidebarService } from './sidebar.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { UserService } from 'src/app/shared/services/api-user.service';
import { map, switchMap, filter, delayWhen } from 'rxjs/operators';
import { Observable, tap, timer } from 'rxjs';


@Component({
    selector: 'app-sidebar',
    templateUrl: './sidebar.component.html',
    styleUrls: ['./sidebar.component.scss'],
    providers: [UserService]
})
export class SidebarComponent extends BaseComponent {

    isActive: boolean = false;
    collapsed: boolean = false;
    showMenu: string = '';
    pushRightClass: string = 'push-right';
    menus: Menu[] = [];
    @Output() collapsedEvent = new EventEmitter<boolean>();

    constructor(
      route: ActivatedRoute,
      protected trans: TransformMenuNamePipe,
      public router: Router,
      private service: SidebarService,
      private tool: ToolService,
      private logoutService: LogoutService
    ) {
        super(route, trans);
        this.router.events.subscribe(val => {
            if (
                val instanceof NavigationEnd &&
                window.innerWidth <= 992 &&
                this.isToggled()
            ) {
                this.toggleSidebar();
            }
        });
        //以程式控制是否有權限瀏覽,故移除以下funcs
        this.service.convert(this.service.removes).pipe(
            delayWhen(()=>{
              return sessionStorage.getItem('roleFuncCodeList') ? timer(0):timer(1000);
            }),
            switchMap(menu => this.filterMenuItem(menu)
            ),
            map(menu => this.tool.validateMenusNFuncCode(menu)) //沒在layout routing內出現的就會篩掉
        ).subscribe(menus => this.menus = menus)
    }

    ngOnInit() {
        this.service.resetMenuHandle.subscribe(r => {
            if (r) {
                this.service.convert(this.service.removes).pipe(
                    switchMap(menu => this.filterMenuItem(menu)),
                    map(menu => this.tool.validateMenusNFuncCode(menu))//沒在layout routing內出現的就會篩掉
                ).subscribe(menus => this.menus = menus);
            }
        })
    }
    menuClick(menu:Menu) {
        if (menu.enabled) {
            this.menus.filter(m => m.main != menu.main).forEach(m => m.enabled = false);
        }
    }


    /**
     * 20220113新增
     * 把menu(由Tsmp_func取回)的資料跟前端routes source(layout.routing.ts)的id做比對
     * 若menu.func_code無法對應到routes的id，則移除
     */
    // validateMenusNFuncCode(menu: any) {
    //     let layoutRouteData = new Array; // 用來記錄有在layout.routing.ts內註冊的頁面id
    //     const config = this.router.config;

    //     for (let i = 0; i < config.length; i++) {
    //         if ('_loadedConfig' in config[i]) {
    //             if (config[i]['_loadedConfig'].routes[0].children) {
    //                 config[i]['_loadedConfig'].routes[0].children.forEach(childRoute => {
    //                     if ('data' in childRoute) {
    //                         layoutRouteData.push(childRoute.data.id.toUpperCase())
    //                     }
    //                 });
    //             }
    //         }
    //     }
    //     // console.log(layoutRouteData);
    //     // console.log(menu);

    //     menu.forEach((item:Menu) => {
    //         item.subs = item.subs?.filter(subitem => layoutRouteData.find(id =>{
    //           return subitem.name.startsWith('ZA') || subitem.name.startsWith('AC09') || id === subitem.name
    //         }))
    //     });
    //     // console.log('menu:',menu)

    //    menu = menu.filter((item:Menu) =>{
    //       return (item.subs && item.subs.length>0) ? true: false;
    //     })
    //     // console.log('menu:',menu)
    //     return menu;
    // }

    /**
     * 客製化選單 調整選單位置
     * @param data
     */
    private customAdjMenuPosition(data: Menu[]) {
        let labs = data.find(d => d.main === 'LB00');
        let labsIndex = data.findIndex(d => d.main === 'LB00');
        if(labs) {
          data.splice(labsIndex,1)
          data.push(labs)
        };

        //將模組查詢選單搬移到API管理中,再移除模組管理主選單
        // let ac04 = data.find(d => d.main === 'AC04');
        let ac03 = data.find(d => d.main === 'AC03');
        if (ac03 && ac03.subs && ac03.subs.length) {
            let ac0402 = ac03.subs.find(x => x.name === 'AC0402');
            if (ac0402) {
                // if (ac03) {
                //調整順序
                //如果 getAcConf edition: Enterprise、net: true、stable: true
                //(ac0301 -> ac0402 -> ac0311 -> ac0315 -> ac0316 -> ac0412 -> ac0802 -> ac0422 -> ac0432)
                //如果 getAcConf edition: Enterprise、net: false、stable: true
                //(ac0301 -> ac0402 -> ac0311 -> ac0315 -> ac0316 -> ac0412 -> ac0802)
                //如果 getAcConf edition: Express、net: true、stable: true
                //(ac0301 -> ac0402 -> ac0316 -> ac0412 -> ac0422 -> ac0432)
                //如果 getAcConf edition: Express、net: false、stable: true
                //(ac0301 -> ac0402 -> ac0316 -> ac0412)
                // let last = ac03.subs.pop();
                let ac0311 = ac03.subs.find(x => x.name === 'AC0311');
                let ac0422 = ac03.subs.find(x => x.name === 'AC0422');
                let ac0802 = ac03.subs.find(x => x.name === 'AC0802');

                if (ac0311) {
                    let ac0402_index = ac03.subs.findIndex(x => x.name === 'AC0402');
                    ac03.subs.splice(ac0402_index, 1);
                    let ac0311_index = ac03.subs.findIndex(x => x.name === 'AC0311');
                    ac03.subs.splice(ac0311_index, 0, ac0402);
                }
                else {
                    let ac0402_index = ac03.subs.findIndex(x => x.name === 'AC0402');
                    ac03.subs.splice(ac0402_index, 1);
                    let ac0316_index = ac03.subs.findIndex(x => x.name === 'AC0316');
                    ac03.subs.splice(ac0316_index, 0, ac0402);
                }
                if (ac0802 && ac0422) {
                    let ac0802_index = ac03.subs.findIndex(x => x.name === 'AC0802');
                    ac03.subs.splice(ac0802_index, 1);
                    let ac0422_index = ac03.subs.findIndex(x => x.name === 'AC0422');
                    ac03.subs.splice(ac0422_index, 0, ac0802);
                }

                // data.splice(data.findIndex(d => d.main === 'AC04'), 1);
                // }
            }
        }

        // 調整AC0509的排序，使其介於在AC0507及AC0508之間
        let ac05 = data.find(d => d.main === 'AC05');
        if (ac05 && ac05.subs && ac05.subs.length) {
            let ac0509 = ac05.subs.find(x => x.name === 'AC0509');
            if(ac0509){
                let ac0509_index = ac05.subs.findIndex(x => x.name === 'AC0509');
                ac05.subs.splice(ac0509_index, 1);
                let ac0587_index = ac05.subs.findIndex(x => x.name === 'AC0508');
                ac05.subs.splice(ac0587_index, 0, ac0509);
            }
        }

        // 調整AC0016、AC0017的排序，使其在表單最後方
        let ac00 = data.find(d => d.main === 'AC00');
        if (ac00 && ac00.subs && ac00.subs.length) {
          let ac0016 = ac00.subs.find(x => x.name === 'AC0016');
          if(ac0016){
            let ac0016_index = ac00.subs.findIndex(x => x.name === 'AC0016');
            ac00.subs.splice(ac0016_index,1);
            ac00.subs.push(ac0016);
          }

          let ac0017 = ac00.subs.find(x => x.name === 'AC0017');
          if(ac0017){
            let ac0017_index = ac00.subs.findIndex(x => x.name === 'AC0017');
            ac00.subs.splice(ac0017_index,1);
            ac00.subs.push(ac0017);
          }

          const ac0018 = ac00.subs.find(x => x.name === 'AC0018');
          if(ac0018){
            const ac0018_index = ac00.subs.findIndex(x => x.name === 'AC0018');
            ac00.subs.splice(ac0018_index,1);
            ac00.subs.push(ac0018);
          }

          const ac0019 = ac00.subs.find(x => x.name === 'AC0019');
          if(ac0019){
            const ac0019_index = ac00.subs.findIndex(x => x.name === 'AC0019');
            ac00.subs.splice(ac0019_index,1);
            ac00.subs.push(ac0019);
          }

          const ac0020 = ac00.subs.find(x => x.name === 'AC0020');
          if(ac0020){
            const ac0020_index = ac00.subs.findIndex(x => x.name === 'AC0020');
            ac00.subs.splice(ac0020_index,1);
            ac00.subs.push(ac0020);
          }

          const ac0021 = ac00.subs.find(x => x.name === 'AC0021');
          if(ac0021){
            const ac0021_index = ac00.subs.findIndex(x => x.name === 'AC0021');
            ac00.subs.splice(ac0021_index,1);
            ac00.subs.push(ac0021);
          }
        }

        let ac02 = data.find(d => d.main === 'AC02');
        if (ac02 && ac02.subs && ac02.subs.length) {


          let ac0226 = ac02.subs.find(x => x.name === 'AC0226');
          if(ac0226){
            let ac0026_index = ac02.subs.findIndex(x => x.name === 'AC0226');
            ac02.subs.splice(ac0026_index,1);
            ac02.subs.push(ac0226);
          }
          let ac0227 = ac02.subs.find(x => x.name === 'AC0227');
          if(ac0227){
            let ac0027_index = ac02.subs.findIndex(x => x.name === 'AC0227');
            ac02.subs.splice(ac0027_index,1);
            ac02.subs.push(ac0227);
          }
          let ac0228 = ac02.subs.find(x => x.name === 'AC0228');
          if(ac0228){
            let ac0028_index = ac02.subs.findIndex(x => x.name === 'AC0228');
            ac02.subs.splice(ac0028_index,1);
            ac02.subs.push(ac0228);
          }
          let ac0229 = ac02.subs.find(x => x.name === 'AC0229');
          if(ac0229){
            let ac0029_index = ac02.subs.findIndex(x => x.name === 'AC0229');
            ac02.subs.splice(ac0029_index,1);
            ac02.subs.push(ac0229);
          }
          let ac0230 = ac02.subs.find(x => x.name === 'AC0230');
          if(ac0230){
            let ac0230_index = ac02.subs.findIndex(x => x.name === 'AC0230');
            ac02.subs.splice(ac0230_index,1);
            ac02.subs.push(ac0230);
          }
          let ac0231 = ac02.subs.find(x => x.name === 'AC0231');
          if(ac0231){
            let ac0031_index = ac02.subs.findIndex(x => x.name === 'AC0231');
            ac02.subs.splice(ac0031_index,1);
            let ac0230_index = ac02.subs.findIndex(x => x.name === 'AC0230');
            ac02.subs.splice(ac0230_index,0,ac0231);
          }

        }

        // 調整np1202的排序，使其介於在np0504及np0512之間
        let np05 = data.find(d => d.main === 'NP05');
        if (np05 && np05.subs && np05.subs.length) {
            let np1202 = np05.subs.find(x => x.name === 'NP1202');
            if(np1202){
                let np1202index = np05.subs.findIndex(x => x.name === 'NP1202');
                np05.subs.splice(np1202index, 1);
                let np0512_index = np05.subs.findIndex(x => x.name === 'NP0512');
                np05.subs.splice(np0512_index, 0, np1202);
            }
        }
        return data;
    }
    filterMenuItem(data: Menu[]): Observable<any> {

        return new Observable(obser => {
            const auths = this.tool.getAuthorities();
            if (auths) {
                let _roleFuncCodeList = this.tool.getRoleFuncCodeList();

                data.forEach((main:Menu) => {
                    main.subs?.forEach((sub:SubMenu) => {
                      let idx = _roleFuncCodeList.findIndex(f => f === sub.name);
                        if (idx >= 0) sub.enabled = true
                    });
                });

                data = this.customAdjMenuPosition(data);

                // 過濾子選項，若子選項都沒開則移除父選項
                data = data.filter(menu=>{
                  if(menu.subs){
                    return menu.subs?.some(sub=> sub.enabled)
                  }
                  return false;
                })

                obser.next(data);

            } else {
                // this.router.navigate(['/login']);
                this.logoutService.logout();
                obser.next();
            }
        })

    }

    eventCalled() {
        this.isActive = !this.isActive;
    }

    // addExpandClass(element: any) {
    //     let $li = $(element).children('li').first();
    //     let open = $li.data('open');
    //     if (open)
    //         $li.find('>ul').slideDown();
    //     else{
    //         $li.find('>ul').slideUp();
    //     }
    //     $li.data('open',!open);
    // }

    isToggled(): boolean {
        const dom: Element = document.querySelector('body')!;
        return dom.classList.contains(this.pushRightClass);
    }

    toggleSidebar() {
        const dom: any = document.querySelector('body');
        dom.classList.toggle(this.pushRightClass);
    }
    toggleCollapsed() {
        this.collapsed = !this.collapsed;
        this.collapsedEvent.emit(this.collapsed);
    }

    rltAndLtr() {
        const dom: any = document.querySelector('body');
        dom.classList.toggle('rtl');
    }

    changeLang(language: string) {
        // this.translate.use(language);
    }

    onLoggedout() {
        sessionStorage.removeItem('isLoggedin');
    }
}

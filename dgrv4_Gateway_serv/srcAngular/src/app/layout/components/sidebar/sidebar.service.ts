import { LogoutService } from './../../../shared/services/logout.service';
import { ToolService } from './../../../shared/services/tool.service';
import { Menu, SubMenu } from './../../../models/menu.model';
import { FuncService } from './../../../shared/services/api-func.service';
import { ActivatedRoute, Router, NavigationEnd } from '@angular/router';
import { Injectable, EventEmitter } from "@angular/core";
import { filter, map } from 'rxjs/operators';
import { Observable, Subject } from 'rxjs';
import { MenuMappingIcon } from 'src/app/models/common.enum';
import { AA0510Resp } from 'src/app/models/api/UtilService/aa0510.interface';
import { AA0101func } from 'src/app/models/api/FuncService/aa0101.interface';

@Injectable()
export class SidebarService {

    // mainId:string = '';
    mainId:Subject<string> = new Subject();
    siderbarEventEmitter: EventEmitter<{ menu: any, actived: any, id:any }>;
    actived: any;
    activeId: string = '';
    acConf: AA0510Resp;
    ACTIVE: string = 'active';
    funcList: Array<AA0101func> = [];
    newFuncList: Array<AA0101func> = [];
    // 移除不顯示在menu上的項目
    removes = [
        'AC0001', 'AC0003', 'AC0004', 'AC0005', 'AC0009', 'AC0011', 'AC0013', 'AC0014',
        'AC0102',
        'AC0201', 'AC0203', 'AC0204', 'AC0205', 'AC0211', 'AC0213', 'AC0214', 'AC0215', 'AC0216', 'AC0217', 'AC0218', 'AC0219', 'AC0220', 'AC0221', 'AC0223', 'AC0224', 'AC0225',
        'AC0302', 'AC0303', 'AC0304', 'AC0305', 'AC0317', 'AC0318',
        'AC0401', 'AC0403', 'AC0404', 'AC0405', 'AC0411', 'AC0413', 'AC0414', 'AC0415', 'AC0421', 'AC0423', 'AC0424', 'AC0425', 'AC0431', 'AC0433', 'AC0434', 'AC0435',
        'AC0505',
        'AC0701', 'AC0703', 'AC0704', 'AC0705',
        'AC0801', 'AC0803', 'AC0804',
        'AC1001', 'AC1003', 'AC1004', 'AC1005',
        'AC1101', 'AC1102', 'AC1103', 'AC1104', 'AC1105', 'AC1106', 'AC1108', 'AC1109', 'AC1110'
    ];
    resetMenuHandle: EventEmitter<any> = new EventEmitter;

    constructor(
        private activatedRoute: ActivatedRoute,
        private router: Router,
        private func: FuncService,
        private toolService: ToolService,
        private logoutService: LogoutService
    ) {
        this.siderbarEventEmitter = new EventEmitter<{ menu: any, actived: any, id:any }>();
        this.siderbarEventEmitter.subscribe(({ menu, actived, id }) => {
          // console.log('first',menu)
            this.clear();
            this.actived = actived;
            if (id) this.activeId = id;
            // $(menu.nativeElement).closest('.list-group').find('.' + this.ACTIVE).removeClass(this.ACTIVE);
            $(actived).addClass(this.ACTIVE)
        });
        const urlSegments = this.router.url.split('/');
        this.activeId = urlSegments[urlSegments.length - 1];
        // this.getId().subscribe(id => {

        //     this.activeId = id;
        //     // console.log(this.activatedRoute.snapshot.params);
        //     // if (!this.activeId){
        //     //     this.activeId = this.activatedRoute.snapshot.params.id;
        //     //     console.log(this.activeId);
        //     // }
        // });
        //處理acConf
        this.acConf = this.toolService.getAcConf();
    }

    /**
     * 重新整理左側選單
     */
    reset() {

        this.toolService.refreshToken().subscribe(rr => {
            this.func.queryAllFunc().subscribe(r => {
                if (this.toolService.checkDpSuccess(r.ResHeader)) {
                    this.toolService.setFuncList(r.RespBody.funcList);
                    this.resetMenuHandle.emit(true)
                }
            });
        });
    }

    getId() {
        return this.router
            .events
            .pipe(
                filter(event => event instanceof NavigationEnd),
                map(() => {
                    let child = this.activatedRoute.firstChild;
                    while (child) {
                        if (child.firstChild) {
                            child = child.firstChild;
                        } else if (child.snapshot.data && child.snapshot.data['id']) {
                            return child.snapshot.data['id'];
                        } else {
                            return null;
                        }
                    }
                    return null;
                })
            );
    }

    //轉換menu為顯示用的格式
    convert(removes?: string[]): Observable<any> {
        return new Observable(obser => {
            this.funcList = this.toolService.getFuncList();

            if (!this.funcList) {
              // this.router.navigateByUrl('/login')
              this.logoutService.logout();
              return;
            };
            this.newFuncList = this.funcList.slice(0);
            if (removes) removes.forEach(r => {
                let idx = this.newFuncList.findIndex(f => f.funcCode === r);
                if (idx >= 0) {
                    this.newFuncList.splice(idx, 1);
                }
            })
            if (this.newFuncList) {
                let mains = this.transform(this.newFuncList);
                obser.next(mains);
            }
        })
    }

    transform(tFuncDetail: Array<AA0101func>, adjMenu: boolean = true) {
        //先取main item
        const dontShowMenus = ['AC04', 'AC08', 'AC11']; // 移除大項目
        if (this.acConf.dp != 1) { // DP 相關 for 市府 menu
            dontShowMenus.push('DP02');
            dontShowMenus.push('DP03');
            dontShowMenus.push('DP04');
            dontShowMenus.push('DP05');
            dontShowMenus.push('DP06');
            dontShowMenus.push('DP07');
            dontShowMenus.push('DP09');
            // dontShowMenus.push('DP10');
            dontShowMenus.push('DP12');
        }
        let filters = tFuncDetail.filter(f => (f.funcCode.length == 4));
        if (adjMenu) filters = filters.filter(f => !dontShowMenus.includes(f.funcCode));
        let mains: Menu[] = filters.map((f, idx) => {

            // let icon_file = MenuMappingIcon[f.funcCode];
            let icon_file = MenuMappingIcon[f.funcCode as keyof typeof MenuMappingIcon];

            if (!icon_file) { icon_file = MenuMappingIcon['Default'] };
            //if (!icon_file) throw '無法對應功能圖示，請先至MenuMappingIcon建立對應enum.';
            let item: Menu = {
                main: f.funcCode,
                icon: `assets/images/${icon_file}.png`,
                enabled: false,
                subs:[] ,
            }
            return item;
            // return {
            //     main: f.funcCode,
            //     icon: `assets/images/${icon_file}.png`,
            //     enabled: false,
            //     subs: []
            // } as Menu
        });
         mains.forEach(main => {
            // console.log('tFuncDetail',tFuncDetail)
            let subs:SubMenu[] = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main).map(f => {
              // console.log('subs item',f);
                return {
                    name: f.funcCode,
                    path: `/${main.main.toLowerCase()}/${f.funcCode.toLowerCase()}`,
                    enabled: false,
                    funcURL: f.funcURL
                } as SubMenu
            })

            //若為ac00的話，須納入ac10xx(該節點沒有父節點)
            if (main.main == "AC00" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "AC1001" || f.funcCode == "AC1002" || f.funcCode == "AC1003" || f.funcCode == "AC1004" || f.funcCode == "AC1005" || f.funcCode == 'AC1202').map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })

            }

            //若為ac02的話，須納入ac11xx(該節點沒有父節點)
            if (main.main == "AC02" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "AC1101" || f.funcCode == "AC1102" || f.funcCode == "AC1103" || f.funcCode == "AC1104" || f.funcCode == "AC1105" || f.funcCode == "AC1106" || f.funcCode == "AC1107" || f.funcCode == "AC1108" || f.funcCode == "AC1109" || f.funcCode == "AC1110" || f.funcCode == "AC1116").map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })
            }

            //若為ac03的話 須納入ac0412、ac0422、ac0432、ac0802(該節點沒父節點)
            if (main.main == "AC03" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "AC0401" || f.funcCode == "AC0402" || f.funcCode == "AC0403" || f.funcCode == "AC0404" || f.funcCode == "AC0405" || f.funcCode == "AC0411" || f.funcCode == "AC0412" || f.funcCode == "AC0413" || f.funcCode == "AC0414" || f.funcCode == "AC0415" || f.funcCode == "AC0421" || f.funcCode == "AC0422" || f.funcCode == "AC0423" || f.funcCode == "AC0424" || f.funcCode == "AC0425" || f.funcCode == "AC0431" || f.funcCode == "AC0432" || f.funcCode == "AC0433" || f.funcCode == "AC0434" || f.funcCode == "AC0435" || f.funcCode == "AC0801" || f.funcCode == "AC0802" || f.funcCode == "AC0803" || f.funcCode == "AC0804").map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })
            }

            //若為ac05的話 須納入ac0702(該節點沒父節點)
            if (main.main == "AC05" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "AC0701" || f.funcCode == "AC0702" || f.funcCode == "AC0703" || f.funcCode == "AC0704" || f.funcCode == "AC0705"|| f.funcCode == "AC0706").map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })
            }

            //若為np05的話，須納入np1202(該節點沒有父節點)
            if (main.main == "NP05" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "NP1202").map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })
            }

            //若為np03的話，須納入np401、np0402(該節點沒有父節點)
            if (main.main == "NP03" && adjMenu) {
                subs = tFuncDetail.filter(f => f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "NP0401" || f.funcCode == "NP0402").map(f => {
                    return {
                        name: f.funcCode,
                        path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                        enabled: false,
                        funcURL: f.funcURL
                    } as SubMenu
                })
            }


             if (main.main == "LB00" && adjMenu) {
              subs = tFuncDetail.filter(f =>f.funcCode.includes(main.main) && f.funcCode != main.main || f.funcCode == "LB0001" || f.funcCode == "LB0002").map(f => {
                  return {
                      name: f.funcCode,
                      path: `/${f.funcCode.includes(main.main) ? main.main.toLowerCase() : f.funcCode.substr(0, 4).toLowerCase()}/${f.funcCode.toLowerCase()}`,
                      enabled: false,
                      funcURL: f.funcURL
                  } as SubMenu
              })
             }
            main.subs = subs;



        })
        // mains = this.transformEdtion(mains); // 由AA0101搭配SQL取得Express & Enterprise & taipeigov版本Menu，所以不必再自己客製filter掉
        // mains = this.transformStable(mains); // 有區分DEV、SIT環境，所以可以移除此邏輯
        mains = this.transformNet(mains);
        // console.log('mains1',mains[0].subs)
        // console.log('mains2',mains[0])
        return mains;
    }

    private transformEdtion(mains: Menu[]): Menu[] {

        if (this.acConf.edition == "Enterprise") {
            // if (this.acConf.edition == "Enterprise" && this.acConf.apiLogWriteRDB == 'true') {
            //     return mains;
            // }
            // if (this.acConf.edition == "Enterprise" && this.acConf.apiLogWriteRDB == 'false') {
            //     // 移除chart.js繪圖
            //     mains = mains.filter(m => m.main.indexOf('AC13') == -1);
            //     return mains;
            // }

            // 移除Express相同報表 API Module
            for (let main of mains) {
                // if (main.main != "NP05") { continue; }
                main.subs = main.subs?.filter(s => s.name != "NP0515");
            }
            // 移除chart.js繪圖
            if (this.acConf.apiLogWriteRDB == 'false') {
                mains = mains.filter(m => m.main.indexOf('AC13') == -1);
            }
            // return mains;
        }
        else {
            //移除DP、NP相關功能
            mains = mains.filter(m => m.main.indexOf("DP") == -1);
            mains = mains.filter(m => m.main.indexOf("NP01") == -1);
            mains = mains.filter(m => m.main.indexOf("NP12") == -1);

            //移除kibana報表
            mains = mains.filter(m => m.main.indexOf('AC09') == -1);

            // 移除 API Module(AC0402)、API組合與設計(AC0315)、部署主機管理(AC0412)、.NET MODULE管理(AC0422)、.NET SITE 管理(AC0432)、排程工作執行列表(AC0504)、稽核日誌(AC0507)、ELK儀表板(AC0508)、Redis即時監控儀錶板(AC0521)、告景設定(AC0702)、API上下架(NP0301)、用戶端註冊(NP0302)、用戶端授權API(NP0303)、簽核關卡維護(NP0402)、事件檢視器(NP0511)、入口網上架現況查詢(NP0512)、週期排程作業(NP0514)
            for (let main of mains) {
                if (main.main != "AC02" && main.main != "AC03" && main.main != "AC05" && main.main != 'AC09' && main.main != 'NP03' && main.main != 'NP03' && main.main != 'NP05') { continue; }
                main.subs = main.subs?.filter(s => s.name != "AC0207" && s.name != "AC0402" && s.name != "AC0315" && s.name != "AC0412" && s.name != "AC0422" && s.name != "AC0432" && s.name != "AC0504" && s.name != "AC0507" && s.name != "AC0508" && s.name != "AC0521" && s.name != "AC0702" && s.name != 'NP0301' && s.name != 'NP0302' && s.name != 'NP0303' && s.name != 'NP0402' && s.name != 'NP0511' && s.name != 'NP0512' && s.name != 'NP0514');
            }
        }

        return mains;
    }

    // private transformStable(mains: Menu[]): Menu[] {
    //     //非穩定版，顯示所有功能，包括開發測試中的功能
    //     if (this.acConf.stable == false) { return mains; }

    //     //目前開發測試中的api  (np03-np0302、np03-np0303)
    //     for (let main of mains) {
    //         if (main.main == "NP03") {
    //             main.subs = main.subs.filter(s => s.name != 'NP0302' && s.name != "NP0303");
    //         }
    //     }

    //     return mains;
    // }

    private transformNet(mains: Menu[]): Menu[] {
        //.NET版本，顯示有含.NET的功能
        if (this.acConf.net == 'true') { return mains; }

        for (let main of mains) {
            if (main.main != "AC03") { continue; }
            main.subs = main.subs?.filter(s => s.name != "AC0422" && s.name != "AC0432");
        }

        return mains;
    }

    clear() {
        $(this.actived).removeClass(this.ACTIVE)
    }

    setMenuMainId(_id:string) {
      this.mainId.next(_id);
    }

    getMenuMainId() {
      return this.mainId.asObservable();
    }

}

import { BaseComponent } from 'src/app/layout/base-component';
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { SiteDetailComponent } from 'src/app/layout/ac04/ac0432/site-detail/site-detail.component';
import { SiteInfo, Req_0412 } from 'src/app/models/api/ModuleService/naa0412.interface';
import { ModuleService } from 'src/app/shared/services/api-module.service';
import { MessageService } from 'primeng/api';
import { Req_0414 } from 'src/app/models/api/ModuleService/naa0414.interface';
import { Req_0415 } from 'src/app/models/api/ModuleService/naa0415.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { TranslateService } from '@ngx-translate/core';
import { ModuleStatusPipe } from 'src/app/shared/pipes/module.status.pipe';
import { isNullOrUndefined } from 'util';

@Component({
    selector: 'app-ac0432',
    templateUrl: './ac0432.component.html',
    styleUrls: ['./ac0432.component.css'],
    providers: [ModuleService, MessageService, ModuleStatusPipe]
})
export class Ac0432Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') dialog: DialogComponent;

    dialogWidth: number;
    dialogTitle: string;
    data: SiteInfo[];
    rowCount: number = 0;
    currentSite: SiteInfo = null;
    inactive: string;
    active: string;
    queryModuleSiteId: number = 0;
    cols: ({ field: string; header: string; type?: undefined; } | { field: string; header: string; type: ModuleStatusPipe; })[];

    constructor(
        protected route: ActivatedRoute,
        protected tr: TransformMenuNamePipe,
        private toolService: ToolService,
        private moduleService: ModuleService,
        private messageService: MessageService,
        private alertService: AlertService,
        private translateService: TranslateService,
        private stat: ModuleStatusPipe,
        private router: Router,
        private activatedRoute: ActivatedRoute
    ) {
        super(route, tr);
        this.activatedRoute.queryParams.subscribe(params => {
            if (params.siteid) {
                this.queryModuleSiteId = parseInt(params.siteid);
            }
        });
    }

    async ngOnInit() {
        const dict = await this.toolService.getDict(['site_id', 'site_status', 'site_code', 'site_port', 'clr_version', 'module_name', 'module_version', 'memo', 'action', 'active', 'inactive']);
        this.active = dict['active'];
        this.inactive = dict['inactive'];
        this.cols = [
            { field: 'siteId', header: dict['site_id'] },
            { field: 'status', header: dict['site_status'], type: this.stat },
            { field: 'siteCode', header: dict['site_code'] },
            // { field: 'bindingPort', header: dict['site_port'] },
            { field: 'clrVersion', header: dict['clr_version'] },
            // { field: 'moduleName', header: dict['module_name'] },
            { field: 'moduleVersion', header: dict['module_version'] },
            { field: 'siteMemo', header: dict['memo'] }
        ];

        this.loadData();
    }

    loadData(): void {
        let req = {
            siteId: isNullOrUndefined(this.queryModuleSiteId) ? 0 : this.queryModuleSiteId,
            moduleName: "",
            detailFlag: true
        } as Req_0412;
        // console.log('req naa0412 :', req)
        this.moduleService.querySiteList(req).subscribe(res => {
            // console.log('res naa0412:', res)
            if (this.toolService.checkSuccess(res.resHeader)) {
                this.data = res.res_0412.siteInfoList;
                this.rowCount = res.res_0412.listCount;
            }
        });
    }

    create(): void {
        this.update(null);
    }

    async update(item: SiteInfo): Promise<void> {
        const dict = await this.toolService.getDict(['net_site_cr', 'net_site_up']);
        this.dialogWidth = 865;
        this.dialogTitle = item ? dict['net_site_up'] : dict['net_site_cr'];

        this.dialog.open(SiteDetailComponent, {
            data: item, afterCloseCallback: (r) => {
                this.loadData();
            }
        });
    }

    async startStop(item: SiteInfo): Promise<void> {
        let dict = await this.toolService.getDict(['active', 'inactive']);
        this.currentSite = item;
        this.messageService.clear();
        let action = item.status == '1' ? dict['inactive'] : dict['active'];
        dict = await this.toolService.getDict(['cfm_site', 'cfm_pro'], { value: action });
        this.messageService.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_site'], detail: dict['cfm_pro'] });
    }

    async delete(item: SiteInfo): Promise<void> {
        if (item.status == '1') return;
        let dict = await this.toolService.getDict(['cfm_del_site', 'site_id', 'site_code']);
        this.currentSite = item;
        this.messageService.clear();
        this.messageService.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_site'], detail: `${dict['site_id']} : ${this.currentSite.siteId}, ${dict['site_code']} : ${this.currentSite.siteCode}` });
    }

    onStartStopConfirm(): void {
        this.messageService.clear();
        let req = {
            siteId: this.currentSite.siteId,
            siteCode: this.currentSite.siteCode,
            newStatus: this.currentSite.status == "0" ? "1" : "0"
        } as Req_0414;
        // console.log('req naa0414 :', req)
        if (this.currentSite.moduleID != 0) {
            this.moduleService.startStopSite(req).subscribe(res => {
                // console.log('res naa0414 :', res)
                if (this.toolService.checkSuccess(res.resHeader)) {
                    this.loadData();
                }
            });
        }
        else {
            this.translateService.get('alert').subscribe(alert => {
                this.alertService.error(alert.title.error, alert.text.active_net_site_error);
            });
        }
    }

    onDeleteConfirm(): void {
        this.messageService.clear();

        let req = {
            siteId: this.currentSite.siteId,
            siteCode: this.currentSite.siteCode
        } as Req_0415;
        // console.log('req naa0415 :', req)
        this.moduleService.deleteSite(req).subscribe(res => {
            if (this.toolService.checkSuccess(res.resHeader)) {
                this.loadData();
            }
        });
    }

    redirect(moduleName: string) {
        let queryModule = { module: moduleName };
        this.router.navigate(['/ac04/ac0422'], { queryParams: queryModule })
    }

    onReject(): void {
        this.messageService.clear();
    }
}

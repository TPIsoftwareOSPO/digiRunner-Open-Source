import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { moduleList, DPB0040Req } from 'src/app/models/api/DocService/dpb0040.interface';
import { MessageService } from 'primeng/api';
import { DocService } from 'src/app/shared/services/api-doc.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0041Req } from 'src/app/models/api/DocService/dpb0041.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';

@Component({
    selector: 'app-np1201',
    templateUrl: './np1201.component.html',
    styleUrls: ['./np1201.component.css'],
    providers: [MessageService]
})
export class Np1201Component extends BaseComponent implements OnInit {

    form!: FormGroup;
    cols: { field: string; header: string; }[] = [];
    rowCount: number = 0;
    moduleList: moduleList[] = [];
    selectedModules: moduleList[] = [];

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private doc: DocService,
        private tool: ToolService,
        private ngx: NgxUiLoaderService,
        private message: MessageService
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.form = this.fb.group({
            moduleName: new FormControl('')
        });
        this.init();
        let ReqBody = {
            moduleName: null,
            moduleVersion: null
        } as DPB0040Req;
        this.doc.queryModuleLikeList_ignore1298(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['open', 'non_publicise'];
                const dict = await this.tool.getDict(codes);
                this.moduleList = res.RespBody.moduleList;
                this.rowCount = this.moduleList.length;
                this.selectedModules = this.moduleList.filter(md => md.deniedFlag == '1');
                this.moduleList.map(md => {
                    md.deniedString = md.deniedFlag == '1' ? dict['non_publicise'] : dict['open'];
                });
            }
        });
    }

    async init() {
        const codes = ['denied_status', 'module_name', 'module_version'];
        const dict = await this.tool.getDict(codes);
        this.cols = [
            { field: 'deniedString', header: dict['denied_status'] },
            { field: 'moduleName', header: dict['module_name'] },
            // { field: 'moduleVersion', header: dict['module_version'] }
        ];
    }

    loadModuleList() {
        let ReqBody = {
            moduleName: null,
            moduleVersion: null
        } as DPB0040Req;
        this.doc.queryModuleLikeList(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['open', 'non_publicise'];
                const dict = await this.tool.getDict(codes);
                this.moduleList = res.RespBody.moduleList;
                this.rowCount = this.moduleList.length;
                this.selectedModules = this.moduleList.filter(md => md.deniedFlag == '1');
                this.moduleList.map(md => {
                    md.deniedString = md.deniedFlag == '1' ? dict['non_publicise'] : dict['open'];
                });
            }
        });
    }

    moreDate() {
        this.ngx.start();
        let ReqBody = {
            moduleName: this.moduleList[this.moduleList.length - 1].moduleName,
            moduleVersion: this.moduleList[this.moduleList.length - 1].moduleVersion
        } as DPB0040Req;
        this.doc.queryModuleLikeList(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.ngx.stop();
                const codes = ['open', 'non_publicise'];
                const dict = await this.tool.getDict(codes);
                this.moduleList = this.moduleList.concat(res.RespBody.moduleList);
                this.rowCount = this.moduleList.length;
                this.selectedModules = this.moduleList.filter(md => md.deniedFlag == '1');
                this.moduleList.map(md => {
                    md.deniedString = md.deniedFlag == '1' ? dict['non_publicise'] : dict['open'];
                });
            }
        });
    }

    submitForm() {
        this.ngx.start();
        let ReqBody = {
            moduleName: null,
            moduleVersion: null,
            keyword: this.form.get('moduleName')!.value
        } as DPB0040Req;
        this.doc.queryModuleLikeList(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const codes = ['open', 'non_publicise'];
                const dict = await this.tool.getDict(codes);
                this.ngx.stop();
                this.moduleList = res.RespBody.moduleList;
                this.rowCount = this.moduleList.length;
                this.selectedModules = this.moduleList.filter(md => md.deniedFlag == '1');
                this.moduleList.map(md => {
                    md.deniedString = md.deniedFlag == '1' ? dict['non_publicise'] : dict['open'];
                });
            }
        });
    }

    async handleDeniedStatus(event) {
        const codes = ['open', 'non_publicise'];
        const dict = await this.tool.getDict(codes);
        if (event.hasOwnProperty("data")) {
            event.data.deniedString = event.data.deniedString == dict['open'] ? dict['non_publicise'] : dict['open'];
        }
        if (event.hasOwnProperty("checked")) {
            this.moduleList.map(md => md.deniedString = event.checked ? dict['non_publicise'] : dict['open']);
        }
    }

    saveDenied() {
        this.ngx.start();
        let ReqBody = {
            moduleNames: this.moduleNameFormat(this.selectedModules)
        } as DPB0041Req;
        this.doc.saveDeniedModule(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.ngx.stop();
                const codes = ['message.setting', 'denied_status', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.add({ severity: 'success', summary: `${dicts['message.setting']} ${dicts['denied_status']}`, detail: `${dicts['message.setting']} ${dicts['message.success']}!` });
                this.selectedModules = new Array<moduleList>();
                this.loadModuleList();
            }
        });
    }

    moduleNameFormat(apis: moduleList[]): string {
        let moduleName = '';
        for (let [index, value] of apis.entries()) {
            if (index != apis.length - 1) {
                moduleName += value.moduleName + ',';
            }
            else {
                moduleName += value.moduleName;
            }
        }
        return moduleName;
    }
}

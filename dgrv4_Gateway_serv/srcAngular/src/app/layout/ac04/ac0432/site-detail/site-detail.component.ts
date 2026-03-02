import { Component, OnInit, Input } from "@angular/core";
import { ModuleService } from "src/app/shared/services/api-module.service";
import { FormParams } from "src/app/models/api/form-params.interface";
import { SiteInfo } from "src/app/models/api/ModuleService/naa0412.interface";
import { FormGroup, FormBuilder } from "@angular/forms";
import { Req_0411 } from "src/app/models/api/ModuleService/naa0411.interface";
import { ToolService } from "src/app/shared/services/tool.service";
import { Req_0413 } from "src/app/models/api/ModuleService/naa0413.interface";
import { TranslateService } from "@ngx-translate/core";

@Component({
    selector: 'app-site-detail',
    templateUrl: './site-detail.component.html',
    styleUrls: ['./site-detail.component.css'],
    providers: [ModuleService]
})
export class SiteDetailComponent implements OnInit {

    @Input() data: FormParams;
    @Input() close: Function;

    siteInfo: SiteInfo;
    form: FormGroup;
    submitName: string;
    sites: Array<{ label: string, value: string }> = new Array();

    constructor(
        private moduleService: ModuleService,
        private fb: FormBuilder,
        private tool: ToolService,
        private translate: TranslateService
    ) { }

    async ngOnInit() {
        this.siteInfo = this.data.data;
        this.translate.get('plz_chs').subscribe(text => {
            this.sites.push({ label: text, value: "0" }, { label: 'v4.0.30319', value: 'v4.0.30319' }, { label: 'v2.0.50727', value: 'v2.0.50727' }, { label: 'none', value: 'none' });
        })
        this.init();
        const codes = ['button.create', 'button.update']
        const dict = await this.tool.getDict(codes);
        this.submitName = (this.siteInfo) ? dict['button.update'] : dict['button.create'];
    }

    init() {
        this.form = this.fb.group({
            siteCode: this.siteInfo == null ? "" : this.siteInfo.siteCode.replace("nev.", ""),
            siteMemo: this.siteInfo == null ? "" : this.siteInfo.siteMemo,
            clrVersion: ["0"]
        });
        this.form.get('clrVersion').setValue('0');

        if (this.siteInfo != null) {
            this.form.get("siteCode").disable();
            this.form.get('clrVersion').setValue(this.siteInfo.clrVersion);
            this.form.get("clrVersion").disable();
        }
    }

    submitForm() {
        if (this.siteInfo)
            this.update();
        else
            this.create();
    }

    create(): void {
        let req = {
            siteCode: "nev." + this.form.get("siteCode").value,
            siteMemo: this.form.get("siteMemo").value,
            clrVersion: this.form.get('clrVersion').value
        } as Req_0411;
        // console.log('req naa0411 :', req)
        this.moduleService.addSite(req).subscribe(res => {
            // console.log('res naa0411:', res)
            if (this.tool.checkSuccess(res.resHeader)) {
                this.close();
                // let reqq = {
                //     siteId: res.res_0411.siteId,
                //     siteCode: "",
                //     detailFlag: true
                // } as Req_0412;
                // console.log('req naa0412 :', reqq)
                // this.moduleService.querySiteList(reqq).subscribe(ress => {
                //     console.log('res naa0412:', ress)
                //     if (this.tool.checkSuccess(ress.resHeader)) {
                // let reqqq = {
                //     siteId: ress.res_0412.siteInfoList[0].siteId,
                //     siteCode: ress.res_0412.siteInfoList[0].siteCode,
                //     siteMemo: '',
                //     protocolType: ress.res_0412.siteInfoList[0].protocolType,
                //     bindingIp: '192.168.1.156',
                //     bindingPort: ress.res_0412.siteInfoList[0].bindingPort,
                //     clrVersion: ress.res_0412.siteInfoList[0].clrVersion
                // } as Req_0413;
                // console.log('req naa0413 :', reqqq)
                // this.moduleService.updateSite(reqqq).subscribe(resss => {
                //     if (this.tool.checkSuccess(resss.resHeader)) {
                //         this.close();
                //     }
                // });
                //     }
                // });
            }
        });
    }

    update(): void {
        // console.log('siteInfo :', this.siteInfo)
        let req = {
            siteId: this.siteInfo.siteId,
            siteCode: this.siteInfo.siteCode,
            siteMemo: this.form.get("siteMemo").value,
            protocolType: this.siteInfo.protocolType,
            bindingIp: this.siteInfo.bindingIp,
            bindingPort: this.siteInfo.bindingPort,
            clrVersion: this.siteInfo.clrVersion
        } as Req_0413;
        // console.log('req naa0413 :', req)
        this.moduleService.updateSite(req).subscribe(res => {
            if (this.tool.checkSuccess(res.resHeader)) {
                this.close();
            }
        });
    }
}
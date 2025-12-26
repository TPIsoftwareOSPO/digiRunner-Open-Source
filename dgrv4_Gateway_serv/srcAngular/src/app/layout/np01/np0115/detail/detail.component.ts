import { Component, OnInit, Input } from '@angular/core';
import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { SitemapService } from 'src/app/shared/services/api-sitemap.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPF0011Node } from 'src/app/models/api/SiteMapService/dpf0011.interface';
import { DPB0033Req } from 'src/app/models/api/SiteMapService/dpb0033.interface';
import { DPB0034Req } from 'src/app/models/api/SiteMapService/dpb0034.interface';

@Component({
    selector: 'app-detail',
    templateUrl: './detail.component.html',
    styleUrls: ['./detail.component.css']
})
export class DetailComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;
    form!: FormGroup;
    tree!: DPF0011Node;
    operate!: FormOperate;
    linkUrl: string = '';

    constructor(
        private fb: FormBuilder,
        private _sitemap: SitemapService,
        private _alert: AlertService,
        private _tool: ToolService
    ) { }

    ngOnInit() {
        this.tree = this.data.data;
        this.operate = this.data.operate!;
        console.log(this.tree)
        this.init();
    }

    public init(): void {
        this.linkUrl = `${location.protocol}//${location.hostname}:${location.port}/tsmpdp/`;
        if (this.tree) {
            this.form = this.fb.group({
                siteId: this.tree.siteId,
                siteDesc: this.tree.siteDesc,
                siteUrl: this.tree.siteUrl,
                siteParentId: this.tree.siteParentId
            });
            // console.log(this.operate);
            // console.log(FormOperate.create);
            if (this.operate == FormOperate.create) {
                this.form.get("siteDesc")!.setValue("");
                this.form.get("siteUrl")!.setValue("");
            }
        }
        else {
            this.form = this.fb.group({
                siteId: new FormControl(0),
                siteDesc: new FormControl('TSMP入口網'),
                siteUrl: new FormControl('#/'),
                siteParentId: new FormControl(0)
            });
            this.form.get('siteDesc')!.disable();
            this.form.get('siteUrl')!.disable();
        }
    }

    async executeSend(req: DPB0034Req) {
        return new Promise<any>(async (resolve) => {
            const codes = ['dialog.warn', 'org_name_required', 'link_required'];
            const dict = await this._tool.getDict(codes);
            if (req.siteDesc == "") { this._alert.ok(dict['dialog.warn'], dict['org_name_required']); return; }
            if (req.siteUrl == "") { this._alert.ok(dict['dialog.warn'], dict['link_required']); return; }
            switch (this.operate) {
                case FormOperate.create:
                    let ReqBody = {
                        siteParentId: req.siteId,
                        siteDesc: req.siteDesc,
                        siteUrl: req.siteUrl
                    } as DPB0033Req
                    this._sitemap.addNode(ReqBody).subscribe(res => {
                        resolve(this._tool.checkDpSuccess(res.ResHeader))
                    });
                    break;
                case FormOperate.update:
                    let updateReqBody = {
                        siteId: req.siteId,
                        siteDesc: req.siteDesc,
                        siteUrl: req.siteUrl
                    } as DPB0034Req
                    this._sitemap.updateNodeById(updateReqBody).subscribe(res => {
                        resolve(this._tool.checkDpSuccess(res.ResHeader));
                    });
                    break;
            }
        })

    }

    public async send(): Promise<void> {
        let siteId: number = this.form.get("siteId")!.value;
        let siteDesc: string = this.form.get("siteDesc")!.value;
        let siteUrl: string = this.form.get('siteUrl')!.value.indexOf('#/') != 0 ? '#/' + this.form.get('siteUrl')!.value : this.form.get('siteUrl')!.value;
        await this.executeSend({ siteId: siteId, siteDesc: siteDesc, siteUrl: siteUrl });

    }
}

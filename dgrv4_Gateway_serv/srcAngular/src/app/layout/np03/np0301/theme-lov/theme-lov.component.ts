import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, OnInit, Input } from '@angular/core';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { of } from 'rxjs';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0076RespItem, DPB0076Req } from 'src/app/models/api/LovService/dpb0076.interface';
import { LovService } from 'src/app/shared/services/api-lov.service';

@Component({
    selector: 'app-theme-lov',
    templateUrl: './theme-lov.component.html',
    styleUrls: ['./theme-lov.component.css']
})
export class ThemeLovComponent implements OnInit {

    @Input() data!: FormParams;
    @Input() close!: Function;

    cols: { field: string; header: string; }[] = [];
    keyword: string = '';
    dataList: Array<DPB0076RespItem> = new Array();
    selected: Array<DPB0076RespItem> = new Array();
    rowcount: number = 0;

    constructor(
        private tool: ToolService,
        private lov: LovService,
        private ref: DynamicDialogRef,
        private config: DynamicDialogConfig
    ) { }

    async ngOnInit() {
      console.log(this.config)
        const code = ['theme_name'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'themeName', header: dict['theme_name'] }
        ];

        this.dataList = this.config.data.data.dataList;
        this.rowcount = this.dataList.length;
        this.keyword = this.config.data.data.keyword;
        switch (this.config.data && this.config.data.operate) {
            case FormOperate.create:
                this.selected = this.config.data.data.selectedThemes;
                break;
            case FormOperate.update:
                this.config.data.data.selectedThemes.map(theme => {
                    this.selected = this.selected.concat(this.dataList.filter(item => item.themeId == theme.themeId));
                });
                break;
        }
    }

    searchThemes() {
        this.dataList = [];
        let ReqBody = {
            keyword: this.keyword
        } as DPB0076Req;
        this.lov.queryThemeLov(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    moreData() {
        let ReqBody = {
            themeId: this.dataList[this.dataList.length - 1].themeId,
            keyword: this.keyword
        } as DPB0076Req;
        this.lov.queryThemeLov(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    chooseThemes() {

        // if (this.close) this.close(of(this.selected));
        this.ref.close(this.selected)
    }
}

import { DPB0124Req } from './../../../models/api/ServerService/dpb0124.interface';
import { ServerService } from './../../../shared/services/api-server.service';
import { DPB0126Req } from './../../../models/api/ServerService/dpb0126.interface';
// import { IndexService } from 'srcAngular/app/shared/services/api-index.service';
// import { SearchService } from 'srcAngular/app/shared/services/api-serach.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../../shared/validator-functions';
import * as dayjs from 'dayjs';
import { MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { IndexStatusPipe } from 'src/app/shared/pipes/index-status.pipe';
import { UTCDatetimeFormatPipe } from 'src/app/shared/pipes/utc-datetime.format.pipe';
import { DPB0125Req } from 'src/app/models/api/ServerService/dpb0125.interface';

@Component({
    selector: 'app-ac0104',
    templateUrl: './ac0104.component.html',
    styleUrls: ['./ac0104.component.css'],
    providers: [IndexStatusPipe, UTCDatetimeFormatPipe]
})
export class Ac0104Component extends BaseComponent implements OnInit {

    form: FormGroup;
    dateSmaxDate: Date = new Date();
    dateEminDate: Date = new Date();
    indexs: { label: string, value: string }[] = [];
    cols: ({ field: string; header: string; type?: undefined; } | { field: string; header: string; type: IndexStatusPipe; } | { field: string; header: string; type: UTCDatetimeFormatPipe; })[] = [];
    data: Array<Object> = new Array<Object>();
    rowcount: number = 0;
    selected: Array<Object> = new Array<Object>();

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        // private searchService: SearchService,
        private idxStat: IndexStatusPipe,
        private datetime_format: UTCDatetimeFormatPipe,
        // private idxService: IndexService,
        private messageService: MessageService,
        private ngxService: NgxUiLoaderService,
        private serverService: ServerService,
    ) {
        super(route, tr);
        this.form = this.fb.group({
          dateS: new FormControl('', [ValidatorFns.requiredValidator()]),
          timePickerS: new FormControl({ hour: 0, minute: 0 }),
          dateE: new FormControl('', [ValidatorFns.requiredValidator()]),
          timePickerE: new FormControl({ hour: 0, minute: 0 }),
          indexName: new FormControl(null, [ValidatorFns.requiredValidator()])
      });

    }

    ngOnInit() {

        this.cols = [
            { field: 'index', header: 'Name' },
            { field: 'health', header: 'Health', type: this.idxStat },
            { field: 'status', header: 'Status' },
            { field: 'pri', header: 'Primaries' },
            { field: 'docs.count', header: 'Docs Count' },
            { field: 'ss', header: 'Storage Size' },
            { field: 'creation.date.string', header: 'Create Time', type: this.datetime_format }
        ];

        let req = { } as DPB0126Req;
        this.serverService.queryAllIndex(req).subscribe( res => {
            if (this.tool.checkDpSuccess(res.ResHeader))
            {
              if(res.RespBody.indexList){
                for (let item of res.RespBody.indexList) {
                    this.indexs.push({ label: item, value: item });
                }
              }
            }
        })

        //如果由全文檢索導引過來，自動Query,日期預設前七天;else 日期預設當天凌晨至now
        let param = this.route.snapshot.data;
        let dateE = new Date();

        if (param['data'] && param['data'].fulltext) {
            let dateS = this.tool.addDay(dateE, -7);
            this.form.get('dateS')!.setValue(dateS);
            this.form.get('dateE')!.setValue(dateE);
        }
        else {
            let dateS = new Date();
            dateS.setHours(0, 0, 0, 0);
            this.form.get('dateS')!.setValue(this.tool.addDay(dateS, -1));
            this.form.get('dateE')!.setValue(this.tool.addDay(dateS, 1));
        }

        this.form.get('dateS')!.valueChanges.subscribe(time => {
            this.dateEminDate = new Date(time);
        });
        this.form.get('dateE')!.valueChanges.subscribe(time => {
            this.dateSmaxDate = new Date(time);
        });
    }

    submitForm() {
        this.ngxService.start();
        this.data = [];
        this.selected = [];
        this.rowcount = this.data.length;

        let req = {
            timeS: this.tool.formateDate(this.tool.addDay(this.dateS!.value, -1)),
            timeE: this.tool.formateDate(this.dateE!.value),
            idxName: this.indexName!.value ? this.indexName!.value : ''
        } as DPB0125Req;
        this.serverService.getIndex(req).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                // this.data = res.RespBody.listObject ? res.RespBody.listObject : [];
                this.data = res.RespBody.listObject??[];
                this.rowcount = this.data.length;
            }
            this.ngxService.stop();
        });
    }

    updateIndexStatus(action) {
        this.ngxService.start();
        let _listESObj:any = [];
        this.selected.map(item => {
            _listESObj.push(item["index"]);
        });

        let req = {
            indexList: _listESObj,
            isOpen: this.tool.Base64Encoder(this.tool.BcryptEncoder(JSON.stringify(action))) + ',' + this.isOpen(action)
        } as DPB0124Req;

        this.serverService
          .updateIndexOpenOrClose(req)
          .subscribe(async (res) => {
            const code = ['message.update', 'message.success'];
            const dict = await this.tool.getDict(code);
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']} Index Status`,
              detail: `${dict['message.update']} ${dict['message.success']}`,
            });
            this.submitForm();
          });

    }

    onClearClick(ctrlname: string) {
        this.form.get(ctrlname)!.setErrors({ error: 'required' });
    }
    onTodayClick(ctrlname: string) {
        this.form.get(ctrlname)!.setValue(new Date());
    }
    colStyle(col): any {
        switch (col.field) {
            case 'creation.date.string':
            case 'index':
                return { width: '100px' };
                break;
            default:
                return { width: '50px' };
                break;
        }
    }

    transformData(rowData: any): string {
        if (typeof rowData != 'string') {
            return '';
        }
        else {
            return rowData;
        }
    }
    isOpen(action) {
        if (action == 0) {
            return '1'
        }
        else if (action == 1) {
            return '0'
        }
        return '';
    }

    public get dateS() { return this.form.get('dateS'); }
    public get timePickerS() { return this.form.get('timePickerS'); }
    public get dateE() { return this.form.get('dateE'); }
    public get timePickerE() { return this.form.get('timePickerE'); }
    public get indexName() { return this.form.get('indexName'); }

}

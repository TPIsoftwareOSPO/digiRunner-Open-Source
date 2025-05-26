import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0106RespItem, DPB0106Req } from 'src/app/models/api/EventService/dpb0106.interface';
import * as dayjs from 'dayjs';
import { EventService } from 'src/app/shared/services/api-event.service';
import { FormOperate } from 'src/app/models/common.enum';
import { TranslateService } from '@ngx-translate/core';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { DPB0107Req } from 'src/app/models/api/EventService/dpb0107.interface';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { EventDetailComponent } from './event-detail/event-detail.component';
import { EventFlagPipe } from 'src/app/shared/pipes/event-flag.pipe';
import { DPB0108Req } from 'src/app/models/api/EventService/dpb0108.interface';
import { MessageService } from 'primeng/api';
import { DPB0109Req } from 'src/app/models/api/EventService/dpb0109.interface';

@Component({
    selector: 'app-np0511',
    templateUrl: './np0511.component.html',
    styleUrls: ['./np0511.component.css'],
    providers: [EventFlagPipe]
})
export class Np0511Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    form: FormGroup;
    cols: ({ field: string; header: string; width: string; type?: undefined; } | { field: string; header: string; width: string; type: EventFlagPipe; })[] = [];
    rowcount: number = 0;
    dataList: Array<DPB0106RespItem> = new Array<DPB0106RespItem>();
    formOperate = FormOperate;
    dialogTitle: string = '';

    constructor(
         route: ActivatedRoute,
         tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private eventService: EventService,
        private evnFlagPipe: EventFlagPipe,
        private translate: TranslateService,
        private message: MessageService
    ) {
        super(route, tr);
        this.form = this.fb.group({
          keyword: new FormControl(''),
          startDate: new FormControl(''),
          endDate: new FormControl('')
      });
    }

    async ngOnInit() {

        this.converDateInit();
        const code = ['keep', 'archive', 'event_id', 'event_type', 'event_name', 'module_name', 'module_version', 'event_msg', 'create_time'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'keepFlag', header: dict['keep'], width: '5%', type: this.evnFlagPipe },
            { field: 'archiveFlag', header: dict['archive'], width: '5%', type: this.evnFlagPipe },
            { field: 'eventId', header: dict['event_id'], width: '5%' },
            { field: 'eventTypeName', header: dict['event_type'], width: '10%' },
            { field: 'eventName', header: dict['event_name'], width: '10%' },
            { field: 'infoMsg', header: dict['event_msg'], width: '30%' },
            { field: 'moduleName', header: dict['module_name'], width: '10%' },
            { field: 'moduleVersion', header: dict['module_version'], width: '5%' },
            { field: 'createDateTime', header: dict['create_time'], width: '10%' }
        ];
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            keyword: this.keyword!.value
        } as DPB0106Req;
        this.eventService.queryEventByDateLike_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    converDateInit() {
        let date = new Date();
        this.startDate!.setValue(this.tool.addDay(date, -6));
        this.endDate!.setValue(date);
    }

    submitForm() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            keyword: this.keyword!.value
        } as DPB0106Req;
        this.eventService.queryEventByDateLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
        });
    }

    showDialog(rowData: DPB0106RespItem, operation: FormOperate) {
        const codes = ['detail'];
        this.translate.get(codes).pipe(
            switchMap(dict => this.openDialog$(rowData, operation, dict))
        ).subscribe();
    }

    openDialog$(rowData: DPB0106RespItem, operation: FormOperate, dict: any): Observable<boolean> {
        let ReqBody = {
            eventId: rowData.eventId
        } as DPB0107Req; return Observable.create(obser => {
            switch (operation) {
                case FormOperate.detail:
                    this.eventService.queryEventByPk(ReqBody).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            this.dialogTitle = dict['detail'];
                            this._dialog.width = 800;
                            this._dialog.open(EventDetailComponent, res.RespBody);
                            obser.next(true);
                        }
                    });
                    break;
            }
        });
    }

    keepEvent(rowData: DPB0106RespItem, index: number) {
        let ReqBody = {
            eventId: rowData.eventId,
            keepFlag: rowData.keepFlag == 'Y' ? 'N' : 'Y'
        } as DPB0108Req;
        this.eventService.keepEventByPk(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                rowData.keepFlag = rowData.keepFlag == 'Y' ? 'N' : 'Y';
                let flagSrc = this.evnFlagPipe.transform(rowData.keepFlag == 'Y' ? 'N' : 'Y');
                $("#keepFlag_" + index).attr("src", flagSrc);
                const codes = ['keep', 'event_log', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.clear();
                this.message.add({ severity: 'success', summary: `${dicts['keep']} ${dicts['event_log']}`, detail: `${dicts['keep']} ${dicts['message.success']}!` });
            }
            else { // 更新失敗才刷新
                this.submitForm();
            }
        });
    }

    archiveEvent(rowData: DPB0106RespItem, index: number) {
        let ReqBody = {
            eventId: rowData.eventId,
            archiveFlag: rowData.archiveFlag == 'Y' ? 'N' : 'Y'
        } as DPB0109Req;
        this.eventService.archiveEventByPk(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                rowData.archiveFlag = rowData.archiveFlag == 'Y' ? 'N' : 'Y';
                let flagSrc = this.evnFlagPipe.transform(rowData.archiveFlag == 'Y' ? 'N' : 'Y');
                $("#archiveFlag_" + index).attr("src", flagSrc);
                const codes = ['archive', 'event_log', 'message.success'];
                const dicts = await this.tool.getDict(codes);
                this.message.clear();
                this.message.add({ severity: 'success', summary: `${dicts['archive']} ${dicts['event_log']}`, detail: `${dicts['archive']} ${dicts['message.success']}!` });
            }
            else { // 更新失敗才刷新
                this.submitForm();
            }
        });
    }

    moreData() {
        let ReqBody = {
            eventId: this.dataList[this.dataList.length - 1].eventId,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            keyword: this.keyword!.value
        } as DPB0106Req;
        this.eventService.queryEventByDateLike(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    async copyInfoMsg(infoMsg: string) {
        const code = ['copy', 'data', 'message.success'];
        const dict = await this.tool.getDict(code);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = infoMsg;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
    }

    public get keyword() { return this.form.get('keyword'); };
    public get startDate() { return this.form.get('startDate'); };
    public get endDate() { return this.form.get('endDate'); };

}

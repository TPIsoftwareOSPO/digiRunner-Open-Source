import { DPB0285WebhookLogItem } from './../../../models/api/ServerService/dpb0285.interface';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DPB0280Req } from 'src/app/models/api/ServerService/dpb0280.interface';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0285Req } from 'src/app/models/api/ServerService/dpb0285.interface';

@Component({
  selector: 'app-lb0012',
  templateUrl: './lb0012.component.html',
  styleUrls: ['./lb0012.component.css'],
  providers: [ConfirmationService],
})
export class Lb0012Component extends BaseComponent implements OnInit {
  currentTitle = this.title;
  pageNum: number = 1;
  form!: FormGroup;
  tableData: Array<DPB0285WebhookLogItem> = [];
  statusList: { label: string; value: string }[] = [];
  statusListIgnoreAll: { label: string; value: string }[] = [];
  currentAction: string = '';

  selectedItem?: DPB0285WebhookLogItem;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private listService: ListService,
    private messageService: MessageService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private ngxSrvice: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      webhookNotifyId: new FormControl(''),
      keyword: new FormControl(''),
      startDate: new FormControl(),
      endDate: new FormControl(),
      paging: new FormControl(''),
    });

    let date = new Date();
    this.startDate!.setValue(this.toolService.addDay(date, -6));
    this.endDate!.setValue(date);

    this.serverService
      .queryWebhookLogs_ignore1298({
        startDate: this.toolService.setformate(
          this.startDate.value,
          'YYYY/MM/DD'
        ),
        endDate: this.toolService.setformate(this.endDate.value, 'YYYY/MM/DD'),
      })
      .subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.tableData = res.RespBody.webhookLogList;
        }
      });
  }

  queryWebhookLogs() {
    let req = {
      startDate:
        !this.startDate.value || this.startDate.value != ''
          ? this.toolService.setformate(this.startDate.value, 'YYYY/MM/DD')
          : null,
      endDate:
        !this.endDate.value || this.endDate.value != ''
          ? this.toolService.setformate(this.endDate.value, 'YYYY/MM/DD')
          : null,
      keyword: this.keyword.value,
    } as DPB0285Req;
    this.ngxSrvice.start();
    this.serverService.queryWebhookLogs(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.webhookLogList;
      } else this.tableData = [];
      this.ngxSrvice.stop();
    });
  }

  getMoreData() {
    let req = {
      keyword: this.keyword.value,
      startDate:
        !this.startDate.value || this.startDate.value != ''
          ? this.toolService.setformate(this.startDate.value, 'YYYY/MM/DD')
          : null,
      endDate:
        !this.endDate.value || this.endDate.value != ''
          ? this.toolService.setformate(this.endDate.value, 'YYYY/MM/DD')
          : null,
      webhookNotifyLogId:
        this.tableData[this.tableData.length - 1].webhookNotifyLogId,
    } as DPB0280Req;
    this.ngxSrvice.start();
    this.serverService.queryWebhookLogs(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.webhookLogList);
      }
      this.ngxSrvice.stop();
    });
  }

  async resend(rowData: DPB0285WebhookLogItem) {
    const code = ['button.resend', 'message.success'];
    const dict = await this.toolService.getDict(code);

    this.confirmationService.confirm({
      header: dict['button.resend'],
      message: `Webhook Name: ${rowData.notifyName}`,

      accept: () => {
        this.ngxSrvice.start();
        this.serverService
          .resendWebhookNotify({
            webhookNotifyLogId: rowData.webhookNotifyLogId,
          })
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['button.resend']} `,
                detail: `${dict['button.resend']} ${dict['message.success']}!`,
              });
            }
            this.ngxSrvice.stop();
          });
      },
    });
  }

  async changePage(action: string, rowData?: DPB0285WebhookLogItem) {
    // console.log(action, rowData);
    this.currentAction = action;
    const code = ['button.detail'];
    const dict = await this.toolService.getDict(code);
    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'detail':
        this.currentTitle = `${this.title} > ${dict['button.detail']}`;
        this.selectedItem = rowData;
        this.pageNum = 2;
        break;
    }
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get startDate() {
    return this.form.get('startDate')!;
  }
  public get endDate() {
    return this.form.get('endDate')!;
  }
}

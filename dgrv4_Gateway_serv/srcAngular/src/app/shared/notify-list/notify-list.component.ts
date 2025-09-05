import { Component, Input, OnInit } from '@angular/core';
import { ToolService } from '../services/tool.service';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ServerService } from '../services/api-server.service';
import {
  DPB0280Req,
  DPB0280WebhookNotify,
} from 'src/app/models/api/ServerService/dpb0280.interface';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-notify-list',
  templateUrl: './notify-list.component.html',
  styleUrls: ['./notify-list.component.css'],
})
export class NotifyListComponent implements OnInit {
  @Input() close?: Function;

  tableData: Array<DPB0280WebhookNotify> = [];
  selectedItems: Array<DPB0280WebhookNotify> = [];
  form!: FormGroup;
  constructor(
    private toolService: ToolService,
    private ref: DynamicDialogRef,
    private serverService: ServerService,
    private fb: FormBuilder,
    private config: DynamicDialogConfig
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      keyword: new FormControl(''),
    });
    this.queryWebhookNotify();
  }

  queryWebhookNotify() {
    let req = {
      keyword: this.keyword.value,
      enable:
        this.toolService.Base64Encoder(this.toolService.BcryptEncoder('Y')) +
        ',0',
      paging: 'N',
    } as DPB0280Req;
    this.serverService.queryWebhookNotify(req).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.webhookNotifyList;
        if (this.config.data.notifyNameList)
          this.selectedItems = this.tableData.filter((rowdata) => {
            return this.config.data.notifyNameList.includes(rowdata.notifyName);
          });
      } else this.tableData = [];
    });
  }

  confirm() {
    this.ref.close(this.selectedItems.map((item) => item.notifyName));
  }

  cancel() {
    this.ref.close(null);
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
}

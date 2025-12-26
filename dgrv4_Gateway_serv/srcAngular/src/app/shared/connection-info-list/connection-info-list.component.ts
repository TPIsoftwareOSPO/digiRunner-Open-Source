import { Component, OnInit } from "@angular/core";
import { ToolService } from "../services/tool.service";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import { DPB0190RespItem } from "src/app/models/api/ServerService/dpb0190.interface";
import * as dayjs from 'dayjs';
import { ServerService } from "../services/api-server.service";

@Component({
  selector: 'app-connection-info-list',
  templateUrl: './connection-info-list.component.html',
  styleUrls: ['./connection-info-list.component.css'],
})
export class ConnectionInfoListComponent implements OnInit {

  selected:Array<DPB0190RespItem> = [];
  tableData:Array<DPB0190RespItem> = [];

  constructor(
    private toolService: ToolService,
    private serverService:ServerService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig
  ) {}

  ngOnInit(): void {
    this.serverService.queryRdbConnectionInfoList().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.infoList;
      }
    })
  }

  chooseRole() {
    this.ref.close(this.selected);
  }

  cancelRole() {
    this.ref.close(null);
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  }
}

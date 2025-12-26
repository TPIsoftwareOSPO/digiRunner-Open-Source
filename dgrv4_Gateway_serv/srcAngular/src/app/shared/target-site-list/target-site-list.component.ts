import { Component, OnInit } from "@angular/core";
import { ToolService } from "../services/tool.service";
import { DynamicDialogConfig, DynamicDialogRef } from "primeng/dynamicdialog";
import * as dayjs from 'dayjs';
import { ServerService } from "../services/api-server.service";
import { ListService } from "../services/api-list.service";
import { ApiService } from "../services/api-api.service";

@Component({
  selector: 'app-target-site-list',
  templateUrl: './target-site-list.component.html',
  styleUrls: ['./target-site-list.component.css'],
  providers:[ApiService],

})
export class TargetSiteListComponent implements OnInit {

  selected:Array<string> = [];
  tableData:Array<string> = [];

  constructor(
    private apiService: ApiService,
    private toolService: ToolService,
    private serverService:ServerService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
  ) {}

  ngOnInit(): void {
    // this.serverService.queryRdbConnectionInfoList().subscribe(res => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //     this.tableData = res.RespBody.infoList;
    //   }
    // })
    this.apiService.queryAllTargetSitList().subscribe(res=>{
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.targetSiteList
      }
    })
  }

  chooseRole() {
    this.ref.close(this.selected);
  }

  cancelRole() {
    this.ref.close(undefined);
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  }
}

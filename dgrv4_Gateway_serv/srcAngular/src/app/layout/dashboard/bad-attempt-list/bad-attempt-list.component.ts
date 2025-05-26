import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { AA1212BadAttemptItemResp } from 'src/app/models/api/ReportService/aa1212.interface';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-bad-attempt-list',
  templateUrl: './bad-attempt-list.component.html',
  styleUrls: ['./bad-attempt-list.component.css'],
})
export class BadAttemptListComponent implements OnInit {
  badAttemptList:Array<AA1212BadAttemptItemResp> = [];
  constructor(private toolService: ToolService,
     private config: DynamicDialogConfig,
  ) {}

  ngOnInit() {
    if (this.config.data) {
      this.badAttemptList = this.config.data.badAttemptList;
    }
  }
}

import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { ToolService } from 'src/app/shared/services/tool.service';
type EsResponse = {
  resp: string;
  connection: boolean;
};

@Component({
  selector: 'app-report-test-connection',
  templateUrl: './report-test-connection.component.html',
  styleUrls: ['./report-test-connection.component.css'],
})
export class ReportTestConnectionComponent implements OnInit {
  esRespArray: { resp: string; connection: boolean; url: string }[] = [];

  constructor(
    private toolService: ToolService,
    public config: DynamicDialogConfig,
    public ref: DynamicDialogRef
  ) {}

  ngOnInit(): void {    
    if (this.config.data?.esResp) {
      this.esRespArray = Object.entries(this.config.data.esResp).map(
        ([url, value]) => ({
          url,
          ...(value as EsResponse),
        })
      );
    }
  }

  onAccept() {
    this.ref.close();
  }
}

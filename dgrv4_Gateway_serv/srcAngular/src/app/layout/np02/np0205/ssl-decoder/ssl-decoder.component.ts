import { Component, OnInit } from '@angular/core';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SSLDecoderResp } from 'src/app/models/api/ServerService/ssl-decoder.interface';
import { ToolService } from 'src/app/shared/services/tool.service';

@Component({
  selector: 'app-ssl-decoder',
  templateUrl: './ssl-decoder.component.html',
  styleUrls: ['./ssl-decoder.component.css'],
})
export class SslDecoderComponent implements OnInit {
  decodeData?: SSLDecoderResp;
  constructor(
    private toolservice: ToolService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig
  ) {}

  ngOnInit() {
    console.log(this.config.data);
    if (this.config.data.details) this.decodeData = this.config.data.details;
  }
}

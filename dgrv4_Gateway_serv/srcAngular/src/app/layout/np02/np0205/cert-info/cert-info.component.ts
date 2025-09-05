import { Component, OnInit } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { SSLDecoderResp } from 'src/app/models/api/ServerService/ssl-decoder.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { marked, RendererObject } from 'marked';

@Component({
  selector: 'app-cert-info',
  templateUrl: './cert-info.component.html',
  styleUrls: ['./cert-info.component.scss'],
})
export class CertInfoComponent implements OnInit {
  certinfo: string = '';
  compiledMarkdown?: SafeHtml;
  constructor(
    private toolservice: ToolService,
    private ref: DynamicDialogRef,
    private config: DynamicDialogConfig,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit() {
    if (this.config.data.details) this.certinfo = this.config.data.details;

    const rawHtml = marked.parse(this.certinfo);
    if ('string' == typeof rawHtml) {
      
      const parser = new DOMParser();
      const doc = parser.parseFromString(rawHtml, 'text/html');
      //對table標籤增加class
      doc.querySelectorAll('table').forEach((table) => {
        table.classList.add('table', 'table-bordered');
      });

      
      const finalHtml = doc.body.innerHTML;
      this.compiledMarkdown = this.sanitizer.bypassSecurityTrustHtml(finalHtml);
    }
  }
}

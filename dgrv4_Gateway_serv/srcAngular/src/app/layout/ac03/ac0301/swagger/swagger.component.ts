import { MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { HttpClient } from '@angular/common/http';
import { ApiBaseService } from 'src/app/shared/services/api-base.service';
import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { DynamicDialogConfig } from 'primeng/dynamicdialog';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

@Component({
  selector: 'app-swagger',
  templateUrl: './swagger.component.html',
  styleUrls: ['./swagger.component.css']
})
export class SwaggerComponent implements OnInit {

  @Input() data?: object;
  form!: FormGroup;
  // url: SafeResourceUrl = {};
  tarUrl: string = ''
  formatJson:string = '';

  constructor(
    private fb: FormBuilder,
    public config: DynamicDialogConfig,
    private sanitizer: DomSanitizer,

    private httpClient: HttpClient,
    private toolService:ToolService,
    private messageService: MessageService,

  ) {

    this.form = this.fb.group({
      swgUrl: new FormControl('')
    })

  }

  ngOnInit(): void {
    this.formatJson = '';
    // this.url = this.sanitizer.bypassSecurityTrustResourceUrl('');

    if (this.config.data) {
      this.tarUrl = this.config.data.tarUrl == undefined ? "" : this.config.data.tarUrl;
      this.swgUrl.setValue(this.tarUrl);
      // this.url = this.sanitizer.bypassSecurityTrustResourceUrl(this.tarUrl);

      this.httpClient.get(this.tarUrl).subscribe(res=>{
        this.formatJson = JSON.stringify(res,undefined,2);

      })
    }
  }


  async copyData(data: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.toolService.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = data;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.messageService.add({
      severity: 'success',
      summary: `${dict['copy']}`,
      detail: `${dict['copy']} ${dict['message.success']}`,
    });
  }

  public get swgUrl() { return this.form.get('swgUrl')!; };

}

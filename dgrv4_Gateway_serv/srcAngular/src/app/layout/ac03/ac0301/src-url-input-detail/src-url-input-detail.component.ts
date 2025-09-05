import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogService } from 'primeng/dynamicdialog';
import { AiApikeyComponent } from '../../ac0311/ai-apikey/ai-apikey.component';

@Component({
  selector: 'app-src-url-input-detail',
  templateUrl: './src-url-input-detail.component.html',
  styleUrls: ['./src-url-input-detail.component.css'],
})
export class SrcUrlInputDetailComponent implements OnInit {
  @Input() data?: { percent: string; url: string; no: number; isMtls: boolean };
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;
  @Input() isAiGateway: boolean = false;
  @Input() pathTpye: string = '';
  apiTpye: string = '';

  @Output() change: EventEmitter<{
    percent: string;
    url: string;
    no: number;
    isMtls: boolean;
  }> = new EventEmitter();
  @Output() remove: EventEmitter<number> = new EventEmitter();

  form!: FormGroup;
  _uuid: string = '';

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService,
    private dialogService: DialogService
  ) {
    this._uuid = this.toolService.generate_uuid();
  }

  ngOnInit(): void {
    // const dgrProtocol_match = this.toolService.dgrProtocol_match(this.data ? this.data.url :'')
    // let _url = this.data?.isMtls == true ? dgrProtocol_match[dgrProtocol_match.length-1] : this.data?.url;

    this.form = this.fb.group({
      percent: new FormControl(this.data ? this.data.percent : ''),
      url: new FormControl(this.data ? this.data.url : ''),
      isMtls: new FormControl(this.data ? this.data.isMtls : false),
    });

    const dgrProtocol_match = this.toolService.dgrProtocol_match(
      this.url.value
    );
    let isMtls = false;
    if (dgrProtocol_match.length > 0) {
      isMtls = dgrProtocol_match.some((item) => item.includes('mtls'));
      this.isHttpAPIArray(dgrProtocol_match) ? this.apiTpye = 'HTTP_API': '';
    }
    if (isMtls && this.url.value.startsWith('dgr+mtls##')) {
      this.url.setValue(this.url.value.replace('dgr+mtls##', ''), {
        onlySelf: false,
        emitEvent: false,
      });
    }

    this.form.valueChanges.subscribe(
      (res: { percent: string; url: string; no: number; isMtls: boolean }) => {
        if (this.apiTpye != 'HTTP_API') {

        this.url.setValue(
          this.toolService.toggleMtlsFlag(res.url, res.isMtls),
          {
            emitEvent: false,
          }
        );
        }
        this.change.emit({
          percent: res.percent,
          url: this.toolService.toggleMtlsFlag(res.url, res.isMtls),
          no: this.no!,
          isMtls: res.isMtls,
        });
      }
    );
  }

  //確認該條url是否為 http api => 不包含ai-gateway, webhook
  isHttpAPIArray(arr: string[]): boolean {
    return !arr.some((item) => {
      const clean = item.replace(/^\+/, ''); // 移除開頭的 +
      return clean === 'webhook' || clean === 'ai-gateway';
    });
  }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  procAiApikey() {
    const ref = this.dialogService.open(AiApikeyComponent, {
      data: {},
      header: 'AI APIKEY',
      width: '700px',
    });

    ref.onClose.subscribe((res) => {
      if (res) {
        this.url.setValue(`dgr+ai-gateway##dgrv4/ai/prompt/${res.id}`);
      } else {
        this.url.setValue('');
      }
    });
  }

  public get percent() {
    return this.form.get('percent')!;
  }
  public get url() {
    return this.form.get('url')!;
  }
  public get isMtls() {
    return this.form.get('isMtls')!;
  }
}

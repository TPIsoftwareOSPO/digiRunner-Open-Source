import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { ToolService } from 'src/app/shared/services/tool.service';
import { DialogService } from 'primeng/dynamicdialog';
import { AiApikeyComponent } from '../ai-apikey/ai-apikey.component';

@Component({
  selector: 'app-src-url-reg-detail',
  templateUrl: './src-url-reg-detail.component.html',
  styleUrls: ['./src-url-reg-detail.component.css'],
})
export class SrcUrlRegDetailComponent implements OnInit {
  @Input() data?: { percent: string; url: string; no: number; isMtls: boolean };
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;
  @Input() apiTpye: string = '';
  @Input() pathTpye: string = '';

  @Output() change: EventEmitter<{
    percent: string;
    url: string;
    no: number;
    isMtls: boolean;
  }> = new EventEmitter();
  @Output() remove: EventEmitter<number> = new EventEmitter();
  @Output() testApiEvt: EventEmitter<string> = new EventEmitter();

  _uuid: string = '';

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private dialogService: DialogService,
    private toolService: ToolService
  ) {
    this._uuid = this.toolService.generate_uuid();
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      percent: new FormControl(this.data ? this.data.percent : 0),
      url: new FormControl(this.data ? this.data.url : ''),
      isMtls: new FormControl(this.data ? this.data?.isMtls ?? false : false),
    });

    if (this.disabled) {
      this.form.disable();
    } else {
      this.form.enable();
    }
    const dgrProtocol_match = this.toolService.dgrProtocol_match(
      this.url.value
    );
    let isMtls = false;
    if (dgrProtocol_match.length > 0) {
      isMtls = dgrProtocol_match.some((item) => item.includes('mtls'));
    }

    //判斷當只有mtls時，移除input上的dgr+mtls##呈現，只保留checkbox勾選
    if(isMtls && this.url.value.startsWith('dgr+mtls##')){
       this.url.setValue(this.url.value.replace('dgr+mtls##',''),{ onlySelf: false, emitEvent: false,})
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
          // url: res.isMtls ? `dgr+mtls##${res.url}` : res?.url ?? '',
          url: this.toolService.toggleMtlsFlag(res.url, res.isMtls),
          no: this.no!,
          isMtls: res.isMtls,
        });
      }
    );
  }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  testApi() {
    this.testApiEvt.emit(this.url.value);
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
    return this.form.get('percent');
  }
  public get url() {
    return this.form.get('url')!;
  }
  public get isMtls() {
    return this.form.get('isMtls')!;
  }
}

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidatorFn } from '@angular/forms';
import { DPB0201RespItem } from 'src/app/models/api/ServerService/dpb0201.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';
// import * as ValidatorFns from '../../../shared/validator-functions';


@Component({
  selector: 'app-website-setting-row',
  templateUrl: './website-setting-row.component.html',
  styleUrls: ['./website-setting-row.component.css']
})
export class WebsiteSettingRowComponent implements OnInit {

  @Input() readonly?: boolean;
  @Input() data?: { probability: number, url: string, no: number, targetThroughPut?: DPB0201RespItem|undefined };
  @Input() _ref: any;
  @Input() no?: number;

  @Output() change: EventEmitter<{ probability: number, url: string, no: number, rowValid: boolean }> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  @Output() validate: EventEmitter<boolean> = new EventEmitter;

  form!: FormGroup;

  req:number = 0;
  resp:number = 0;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) { }

  async ngOnInit() {

    this.form = this.fb.group({
      probability: new FormControl(this.data ? this.data.probability : 0,),
      url: new FormControl(this.data ? this.data.url : '', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(100)]),
    })

    if(this.data?.targetThroughPut){
      this.req = this.data?.targetThroughPut.req;
      this.resp = this.data?.targetThroughPut.resp;
    }

    // if(this.data?.targetThroughPut) this.form.get("targetThroughPut")?.setValue(this.data?.targetThroughPut)

    // const code = ['validation.websiteAddress'];
    // const dict = await this.toolService.getDict(code);

    // var pattern =
    //   '^(https?:\\/\\/)?' + // 協定
    //   '((([a-z\\d]([a-z\\d-]*[a-z\\d])*)\\.)+[a-z]{2,}|' + // 主機名稱
    //   '((\\d{1,3}\\.){3}\\d{1,3}))' + // IP 地址
    //   '(\\:\\d+)?(\\/[-a-z\\d%_.~+]*)*' + // 端口和路徑
    //   '(\\?[;&a-z\\d%_.~+=-]*)?' + // 查詢字串
    //   '(\\#[-a-z\\d_]*)?$'; // 書籤標記

    // this.url.setValidators([ValidatorFns.requiredValidator(),
    // ValidatorFns.maxLengthValidator(100),
    // ValidatorFns.patternValidator(pattern, dict['validation.websiteAddress'])
    // ]);

    // this.url.setValue(this.data ? this.data.url : '');

    if (this.readonly) {
      this.url.disable();
      this.probability.disable();

    }
    else {
      this.url.updateValueAndValidity();

      this.form.valueChanges.subscribe((res: { probability: number, url: string, no: number }) => {
        this.change.emit({ probability: res.probability, url: res.url, no: this.no!, rowValid: this.form.valid });
      });
    }
  }



  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get probability() { return this.form.get('probability')!; }
  public get url() { return this.form.get('url')!; }
  public get targetThroughPut() { return this.form.get('targetThroughPut')!; }


}

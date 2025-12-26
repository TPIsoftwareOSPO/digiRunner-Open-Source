import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA0106ReqItem } from 'src/app/models/api/FuncService/aa0106.interface';
import { DPB0181LdapDataItem } from 'src/app/models/api/ServerService/dpb0181.interface';
import { DPB0182LdapDataItem } from 'src/app/models/api/ServerService/dpb0182.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';

interface _AA0106ReqItem extends AA0106ReqItem {
  valid: boolean;
  no: number;
}

@Component({
  selector: 'app-locale-func-detail',
  templateUrl: './locale-func-detail.component.html',
  styleUrls: ['./locale-func-detail.component.css']
})
export class LocaleFuncDetailComponent implements OnInit {

  @Input() _ref: any;
  @Input() data?: AA0106ReqItem;
  @Input() no: number = 1;
  @Input() localeData: { label: string; value: string }[] = [];

  @Output() change: EventEmitter<_AA0106ReqItem> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) { }


  async ngOnInit() {
    this.form = this.fb.group({
      locale: new FormControl('', [ValidatorFns.requiredValidator()]),
      funcName: new FormControl('', [ValidatorFns.requiredValidator(),ValidatorFns.maxLengthValidator(50)]),
      funcDesc: new FormControl('',[ValidatorFns.maxLengthValidator(300)]),
    });

    this.form.valueChanges.subscribe((res: AA0106ReqItem) => {
      let changeItem = {
        valid: this.form.valid,
        no: this.no,
        ...res
      } as _AA0106ReqItem;
      this.change.emit(changeItem);
    })

  }

  deleteItem() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get locale() { return this.form.get('locale')!; };
  public get funcName() { return this.form.get('funcName')!; };
  public get funcDesc() { return this.form.get('funcDesc')!; };

}

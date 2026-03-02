import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { UntypedFormBuilder, UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';

interface _keyValueField {
  key: string;
  value: string;
  no?: number;
  valid?: boolean;
}

@Component({
    selector: 'app-key-value-field',
    templateUrl: './key-value-field.component.html',
    styleUrls: ['./key-value-field.component.css'],
    standalone: false
})
export class KeyValueFieldComponent implements OnInit {
  @Input() _ref: any;
  @Input() data?: { key: string; value: string };
  @Input() no: number = 1;
  @Input() _disabled: boolean = false;
  @Input() required: boolean = false;
  // @Input() keyLock: boolean = false;
  // @Input() nonRemove: boolean = false;

  @Output() change: EventEmitter<_keyValueField> = new EventEmitter();
  @Output() remove: EventEmitter<number> = new EventEmitter();

  form!: UntypedFormGroup;

  constructor(private fb: UntypedFormBuilder, private toolService: ToolService) {}

  ngOnInit() {
    this.form = this.fb.group({
      key: new UntypedFormControl(this.data ? this.data.key : ''),
      value: new UntypedFormControl(this.data ? this.data.value : ''),
    });
    if (this._disabled) {
      this.form.disable();
    } else {
      this.form.enable();
    }
    if (this.required) {
      this.value?.setValidators([ValidatorFns.requiredValidator()]);
      this.value?.updateValueAndValidity();
      // this.change.emit({
      //   key: this.key?.value,
      //   value: this.value?.value,
      //   no: this.no,
      //   valid: this.form.valid,
      // });
    }

    this.form.valueChanges.subscribe((res: _keyValueField) => {
      res.no = this.no;
      if (this.required) res.valid = this.form.valid;
      this.change.emit(res);
    });
  }

  deleteItem() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get value() {
    return this.form.get('value');
  }
  public get key() {
    return this.form.get('key');
  }
}

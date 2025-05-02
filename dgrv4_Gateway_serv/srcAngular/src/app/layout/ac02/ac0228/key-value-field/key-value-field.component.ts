import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';


interface _keyValueField {
  key: string,
  value: string,
  no?: number
}

@Component({
  selector: 'app-key-value-field',
  templateUrl: './key-value-field.component.html',
  styleUrls: ['./key-value-field.component.css']
})
export class KeyValueFieldComponent implements OnInit {

  @Input() _ref: any;
  @Input() data?: { key: string, value: string };
  @Input() no: number = 1;
  @Input() _disabled: boolean = false;
  @Input() required: boolean = false;

  @Output() change: EventEmitter<_keyValueField> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) { }

  ngOnInit() {

    this.form = this.fb.group({
      value: new FormControl(this.data ? this.data.value : ''),
      key: new FormControl(this.data ? this.data.key : ''),
    });
    if (this._disabled) {
      this.form.disable();
    } else {
      this.form.enable();
    }

    this.form.valueChanges.subscribe((res: _keyValueField) => {
      res.no = this.no;
      this.change.emit(res);
    })
  }

  deleteItem() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get value() { return this.form.get('value'); };
  public get key() { return this.form.get('key'); };

}

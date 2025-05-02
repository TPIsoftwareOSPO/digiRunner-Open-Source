import { Component, OnInit, ChangeDetectionStrategy, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB9906Item } from 'src/app/models/api/ServerService/dpb9906.interface';
import * as ValidatorFns from 'src/app/shared/validator-functions';

interface Subitems extends DPB9906Item {
  valid: boolean;
}

@Component({
  selector: 'app-subitems',
  templateUrl: './subitems.component.html',
  styleUrls: ['./subitems.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SubitemsComponent implements OnInit {

  @Input() itemIdx: number = 0;
  @Input() itemValue?: DPB9906Item;
  @Output() change: EventEmitter<Subitems> = new EventEmitter;
  @Input() paramSize: number = 0;

  paramSizeItem: any[] = [];
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
  ) { }

  ngOnInit(): void {
    this.paramSizeItem = Array.from({ length: this.paramSize }, (_, index) => index);

    this.form = this.fb.group({
      version: new FormControl(),
      locale: new FormControl(),
      subitemName: new FormControl('', ValidatorFns.requiredValidator()),
    });

    for (let index = 0; index < this.paramSize; index++) {
      this.form.addControl(`params${index}`, new FormControl(this.itemValue?.params[index]))
    }



    if (this.itemValue) {
      this.locale.setValue(this.itemValue.locale);
      this.subitemName.setValue(this.itemValue.subitemName);
      this.version.setValue(this.itemValue.version);
    }

    this.form.valueChanges.subscribe(() => {
      let _params: Array<string> = [];
      for (let index = 0; index < this.paramSize; index++) {
        let value = this.form.get('params' + index)!.value ? this.form.get('params' + index)!.value : '';
        _params.push(value)
      }
      // console.log(_params)
      this.change.emit({
        version: this.version.value,
        locale: this.locale.value,
        subitemName: this.subitemName.value,
        params: _params,
        valid: this.form.valid
      });
    })

  }

  public get version() { return this.form.get('version')!; };
  public get locale() { return this.form.get('locale')!; };
  public get subitemName() { return this.form.get('subitemName')!; };

}

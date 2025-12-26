import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { DPB0233WhitelistItem } from 'src/app/models/api/ServerService/dpb0233.interface';

export interface _DPB0233WhitelistItem extends DPB0233WhitelistItem{
  no:number
}

@Component({
  selector: 'app-white-list-detail',
  templateUrl: './white-list-detail.component.html',
  styleUrls: ['./white-list-detail.component.css']
})
export class WhiteListDetailComponent implements OnInit {

  @Input() data?:DPB0233WhitelistItem;
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;

  @Output() change: EventEmitter<_DPB0233WhitelistItem> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder
  ) { }


  ngOnInit(): void {
    this.form = this.fb.group({
      id: new FormControl({value:this.data ? this.data.id : '',disabled: true}),
      rule: new FormControl(this.data ? this.data.rule : ''),
    })

    this.form.valueChanges.subscribe(() => {
      const res = this.form.getRawValue();
      this.change.emit({ id: res.id, rule: res.rule, no: this.no! });
    });
  }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get id() { return this.form.get('id'); };
  public get rule() { return this.form.get('rule'); };

}

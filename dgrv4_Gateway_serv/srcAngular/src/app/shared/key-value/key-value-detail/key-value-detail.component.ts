import { IKeyValue } from './../key-value.interface';

import { Component, OnInit, Input, ComponentRef, AfterViewInit, SimpleChange, EventEmitter, Output, ChangeDetectionStrategy } from '@angular/core';
import { NgModel, FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from './../../../shared/validator-functions';
@Component({
  selector: 'app-key-value-detail',
  templateUrl: './key-value-detail.component.html',
  styleUrls: ['./key-value-detail.component.css'],
  changeDetection:ChangeDetectionStrategy.OnPush
})
export class KeyValueDetailComponent implements OnInit,AfterViewInit {
  changeLog = [];
  keyvalue :IKeyValue |undefined;
  form:FormGroup;

  ngAfterViewInit(): void {

  }
  @Input() data?:{key:string,value:any,no?:any};
  @Input() _ref :any;
  @Input() no:number|undefined;
  @Output() remove : EventEmitter<number>= new EventEmitter;
  @Output() change : EventEmitter<IKeyValue>= new EventEmitter;
  keyLabel:string|undefined;
  valueLabel:string|undefined;

  constructor(private fb:FormBuilder) {
    this.form = this.fb.group({
      selected:new FormControl(true),
      key : new FormControl(this.data ? this.data.key : '',[ValidatorFns.maxLengthValidator(30)]),
      value : new FormControl(this.data ? this.data.value : '')
    })
  }

  ngOnInit() {
    // this.form = this.fb.group({
    //   key : new FormControl(this.data ? this.data.key : '',[ValidatorFns.maxLengthValidator(30)]),
    //   value : new FormControl(this.data ? this.data.value : '')
    // })
    this.keyvalue = {} as IKeyValue;
    this.form.valueChanges.subscribe((res:{key:string,value:any,selected:boolean}) => {
      this.change.emit({key : res.key , value : res.value , no : this.no, selected: res.selected} as IKeyValue);
    })

  }
  delete($event:any){
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get key() { return this.form.get('key')!; };
  public get value() { return this.form.get('value')!; };
  public get selected() { return this.form.get('selected')!; };


}

import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA0424ReqSrcUrlList } from 'src/app/models/api/ApiService/aa0424.interface';

interface _AA0424ReqSrcUrlList extends AA0424ReqSrcUrlList {
  no: number
}
@Component({
  selector: 'app-api-url-setting-detail',
  templateUrl: './api-url-setting-detail.component.html',
  styleUrls: ['./api-url-setting-detail.component.css'],
})
export class ApiUrlSettingDetailComponent implements OnInit {

  @Input() data?: _AA0424ReqSrcUrlList;
  @Input() ref: any;
  @Input() no!: number;
  @Input() beyoungRange: boolean = false;

  @Output() change: EventEmitter<_AA0424ReqSrcUrlList> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      srcUrl: new FormControl(''),
      isPercentage: new FormControl(false),
      percentage: new FormControl({ value: 0, disabled: true }),
      isReplace: new FormControl(false),
      replaceString: new FormControl({ value: '', disabled: true }),
    })

    this.form.valueChanges.subscribe((res: AA0424ReqSrcUrlList) => {
      let proc = {
        ...res,
        no: this.no
      } as _AA0424ReqSrcUrlList;
      this.change.emit(proc);
    })

    this.isPercentage?.valueChanges.subscribe(res => {
      if (res) {
        this.percentage?.enable();
      } else {
        this.percentage?.disable();
      }
    })
    this.isReplace?.valueChanges.subscribe(res => {
      if (res) {
        this.replaceString?.enable();
      } else {
        this.replaceString?.disable();
      }
    })
  }

  deleteItem() {
    this.ref.destroy();
    this.remove.emit(this.no);
  }

  public get srcUrl() { return this.form.get('srcUrl'); };
  public get isPercentage() { return this.form.get('isPercentage'); };
  public get percentage() { return this.form.get('percentage'); };
  public get isReplace() { return this.form.get('isReplace'); };
  public get replaceString() { return this.form.get('replaceString'); };

}

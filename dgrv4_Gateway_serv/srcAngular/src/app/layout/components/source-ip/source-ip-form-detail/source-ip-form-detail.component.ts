import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA0311RedirectByIpData } from 'src/app/models/api/ApiService/aa0311_v3.interface';

export interface _AA0311RedirectByIpData extends AA0311RedirectByIpData {
  listNum: number;
  allowAll?: boolean;
}

@Component({
  selector: 'app-source-ip-form-detail',
  templateUrl: './source-ip-form-detail.component.html',
  styleUrls: ['./source-ip-form-detail.component.css'],
})
export class SourceIpFormDetailComponent implements OnInit {
  form!: FormGroup;

  @Input() data?: _AA0311RedirectByIpData;
  @Input() ref: any;
  @Input() no: number = 0;
  @Input() disabled: boolean = false;

  @Output() change: EventEmitter<{
    ipForRedirect: string;
    ipSrcUrl: string;
    no: number;
  }> = new EventEmitter();
  @Output() remove: EventEmitter<number> = new EventEmitter();

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      ipForRedirect: new FormControl({
        value: this.data ? this.data.ipForRedirect : '',
        disabled: this?.data?.allowAll ?? false,
      }),
      ipSrcUrl: new FormControl(this.data ? this.data.ipSrcUrl : ''),
    });

    this.form.valueChanges.subscribe(
      (res: { ipForRedirect: string; ipSrcUrl: string }) => {
        this.change.emit({
          ipForRedirect: this?.data?.allowAll ? '0.0.0.0/0' : res.ipForRedirect,
          ipSrcUrl: res.ipSrcUrl,
          no: this.no!,
        });
      }
    );
  }

  deleteItem() {
    this.ref.destroy();
    this.remove.emit(this.no);
  }
}

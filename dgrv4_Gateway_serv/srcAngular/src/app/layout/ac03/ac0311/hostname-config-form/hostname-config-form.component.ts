import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { AA0311RedirectByIpData } from 'src/app/models/api/ApiService/aa0311_v3.interface';

export interface _AA0311RedirectByIpData extends AA0311RedirectByIpData {
  listNum: number;
  allowAll?: boolean;
}

@Component({
  selector: 'app-hostname-config-form',
  templateUrl: './hostname-config-form.component.html',
  styleUrls: ['./hostname-config-form.component.css'],
})
export class HostnameConfigFormComponent implements OnInit {
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

  @Output() testApiEvt: EventEmitter<string> = new EventEmitter();


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
        // console.log(this?.data?.allowAll ? '0.0.0.0/0' : res.ipForRedirect)
        this.change.emit({
          ipForRedirect: this?.data?.allowAll ? '0.0.0.0/0' : res.ipForRedirect,
          ipSrcUrl: res.ipSrcUrl,
          no: this.no!,
        });
      }
    );
  }

  procTestApiEvt(evt) {
    this.testApiEvt.emit(evt);
  }

  deleteItem() {
    this.ref.destroy();
    this.remove.emit(this.no);
  }
}

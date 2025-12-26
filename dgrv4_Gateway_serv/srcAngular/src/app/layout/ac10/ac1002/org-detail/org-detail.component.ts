import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, OnInit, Input } from '@angular/core';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from 'src/app/models/common.enum';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-org-detail',
  templateUrl: './org-detail.component.html',
  styleUrls: ['./org-detail.component.css']
})
export class OrgDetailComponent implements OnInit {

  @Input() data?: FormParams;
  @Input() close?: Function;

  center: boolean = true;

  isManager:boolean = false;

  constructor(
    public ref: DynamicDialogRef,
    public config: DynamicDialogConfig
  ) { }

  ngOnInit() {
    this.isManager = this.config.data.orgId === '100000';
  }

  doUpdate() {

    // if (this.close) {
    //   this.close(
    //     new Observable(obser => {
    //       obser.next({ data: this.data?.data, operate: FormOperate.update })
    //     })
    //   );
    // }
    this.ref.close({ data: this.config.data, operate: FormOperate.update });
  }

  doDelete() {
    this.ref.close({ data: this.config.data, operate: FormOperate.delete });
    // if (this.close) {
    //   this.close(
    //     new Observable(obser => {
    //       obser.next({ data: this.data?.data, operate: FormOperate.delete })
    //     })
    //   );
    // }
  }
}

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { DPB0181LdapDataItem } from 'src/app/models/api/ServerService/dpb0181.interface';
import { DPB0182LdapDataItem } from 'src/app/models/api/ServerService/dpb0182.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';

interface _ldapDataListDetail extends DPB0181LdapDataItem {
  valid: boolean;
  no: number;
}

@Component({
  selector: 'app-ldap-data-list-detail',
  templateUrl: './ldap-data-list-detail.component.html',
  styleUrls: ['./ldap-data-list-detail.component.css']
})
export class LdapDataListDetailComponent implements OnInit {

  @Input() _ref: any;
  @Input() data?: DPB0182LdapDataItem;
  @Input() no: number = 1;
  @Input() _disabled: boolean=false;
  @Input() action:string = '';

  @Output() change: EventEmitter<_ldapDataListDetail> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  ldapPattern: string = '^(ldaps?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]';

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) { }


  async ngOnInit() {
    // console.log('action',this.action)

    this.form = this.fb.group({
      orderNo: new FormControl(this.data ? this.data.orderNo : (this.no) + 1, [ValidatorFns.requiredValidator()]),
      ldapUrl: new FormControl(this.data ? this.data.ldapUrl : '', [ValidatorFns.requiredValidator()]),
      ldapBaseDn: new FormControl(this.data ? this.data.ldapBaseDn : '', [ValidatorFns.requiredValidator()]),
      ldapDn: new FormControl(this.data ? this.data.ldapDn : '', [ValidatorFns.requiredValidator()]),
    });

    const code = ['validation.format'];
    const dict = await this.toolService.getDict(code);

    this.ldapUrl?.setValidators([ValidatorFns.requiredValidator(),ValidatorFns.patternValidator(this.ldapPattern, dict['validation.format'])]);
    if(this._disabled) this.form.disable();
    this.form.valueChanges.subscribe((res: DPB0181LdapDataItem) => {
      let changeItem = {
        valid: this.form.valid,
        no: this.no,
        ...res
      } as _ldapDataListDetail;
      this.change.emit(changeItem);
    })
  }

  deleteItem() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get orderNo() { return this.form.get('orderNo'); };
  public get ldapUrl() { return this.form.get('ldapUrl'); };
  public get ldapBaseDn() { return this.form.get('ldapBaseDn'); };
  public get ldapDn() { return this.form.get('ldapDn'); };

}

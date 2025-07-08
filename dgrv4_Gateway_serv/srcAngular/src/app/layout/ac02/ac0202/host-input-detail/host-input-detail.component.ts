
import { AA0201HostReq, AA0201HostReqAddNo } from '../../../../models/api/ClientService/aa0201.interface';
import { Component, OnInit, Input, AfterViewInit, EventEmitter, Output, ChangeDetectionStrategy } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { ToolService } from 'src/app/shared/services/tool.service';
@Component({
  selector: 'app-host-input-detail',
  templateUrl: './host-input-detail.component.html',
  styleUrls: ['./host-input-detail.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class HostInputDetailComponent implements OnInit, AfterViewInit {

  @Input() data?: AA0201HostReq;
  @Input() _ref: any;
  @Input() hostnums?: number;
  @Input() no?: number;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  @Output() change: EventEmitter<AA0201HostReqAddNo> = new EventEmitter;

  deleteFunc: any;
  maxLength30 = { value: 30 };
  maxLength255 = { value: 255 };
  changeLog = [];
  host?: AA0201HostReqAddNo;
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) {


  }

  async ngOnInit() {


    this.form = this.fb.group({
      hostName: new FormControl(this.data ? this.data.hostName : '', [ValidatorFns.maxLengthValidator(this.maxLength30.value)]),
      hostIP: new FormControl(this.data ? this.data.hostIP : '')
    })

    const code = ['validation.format'];
    const dict = await this.toolService.getDict(code);
    // ^(([a-zA-Z0-9][-a-zA-Z0-9]*.)+[a-zA-Z]{2,}(\\:\\d+)?)$

    this.hostIP?.setValidators([ValidatorFns.maxLengthValidator(this.maxLength255.value),ValidatorFns.patternValidator('(?:\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b)(\\:\\d+)?|(?:\\b[0-9a-fA-F:]+\\b)|(?:\\b[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}\\b)(:\\d+)?', dict['validation.format'])]);
    // this.hostIP?.updateValueAndValidity();
    this.host = {} as AA0201HostReqAddNo;
    this.form.valueChanges.subscribe((res: AA0201HostReq) => {
      // console.log(this.hostIP?.value)
      this.change.emit({ hostIP: res.hostIP.trim(), hostName: res.hostName.trim(), no: this.no } as AA0201HostReqAddNo);
    });
  }

  ngAfterViewInit(): void {

  }
  // onChange(){
  //   this.host.hostName = this.hostName.value;
  //   this.host.hostIP = this.hostIP.value;

  // }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get hostName() { return this.form.get('hostName'); };
  public get hostIP() { return this.form.get('hostIP'); };
}

import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, ValidatorFn } from '@angular/forms';
import { DPB0201RespItem } from 'src/app/models/api/ServerService/dpb0201.interface';
import { SmartOnFhirProxyDiversionDto } from 'src/app/models/api/ServerService/dpb0311.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import * as ValidatorFns from 'src/app/shared/validator-functions';

interface _diversionList extends SmartOnFhirProxyDiversionDto {
  no: number;
  rowValid: boolean;
}

@Component({
  selector: 'app-diversion-list-row',
  templateUrl: './diversion-list-row.component.html',
  styleUrls: ['./diversion-list-row.component.css'],
  standalone: false,
})
export class DiversionListRowComponent implements OnInit {

  @Input() readonly?: boolean;
  @Input() data?: _diversionList;
  @Input() _ref: any;
  @Input() no?: number;

  @Output() change: EventEmitter<{ sofProxyDiversionProbability: number, sofProxyDiversionUrl: string, no: number, rowValid: boolean }> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  @Output() validate: EventEmitter<boolean> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private toolService: ToolService
  ) { }

  async ngOnInit() {

    this.form = this.fb.group({
      sofProxyDiversionProbability: new FormControl(this.data ? this.data.sofProxyDiversionProbability : 0,),
      sofProxyDiversionUrl: new FormControl(this.data ? this.data.sofProxyDiversionUrl : '', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(100)]),
      sofProxyId: new FormControl(this.data ? this.data.sofProxyId : ''),
      sofProxyDiversionId: new FormControl(this.data?.sofProxyDiversionId ? this.data.sofProxyDiversionId : ''),
    })

    if (this.readonly) {
      this.sofProxyDiversionUrl.disable();
      this.sofProxyDiversionProbability.disable();
    }
    else {
      this.sofProxyDiversionUrl.updateValueAndValidity();

      this.form.valueChanges.subscribe((res: { sofProxyDiversionProbability: number, sofProxyDiversionUrl: string, no: number }) => {
        this.change.emit({ sofProxyDiversionProbability: res.sofProxyDiversionProbability, sofProxyDiversionUrl: res.sofProxyDiversionUrl, no: this.no!, rowValid: this.form.valid });
      });
    }
  }



  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get sofProxyDiversionProbability() { return this.form.get('sofProxyDiversionProbability')!; }
  public get sofProxyDiversionUrl() { return this.form.get('sofProxyDiversionUrl')!; }
  public get sofProxyDiversionId() { return this.form.get('sofProxyDiversionId')!; }
  public get sofProxyId() { return this.form.get('sofProxyId')!; }


}

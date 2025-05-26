import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';



@Component({
  selector: 'app-fields-form-detail',
  templateUrl: './fields-form-detail.component.html',
  styleUrls: ['./fields-form-detail.component.css']
})
export class FieldsFormDetailComponent implements OnInit {
  @Input() data?: { field: string, no: number };
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;
  @Input() headerPolicy: string = '0';
  @Input() headerPolicyNum: number = 0;
  @Input() headerPolicyMask: string = '*';

  @Output() change: EventEmitter<{ field: string, no: number }> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  // @Output() testApiEvt: EventEmitter<string> = new EventEmitter;

  form!: FormGroup;

  fieldExp: string = '';

  constructor(
    private fb: FormBuilder,
    private toolService:ToolService
  ) { }

  ngOnInit(): void {

    this.form = this.fb.group({
      field: new FormControl(this.data ? this.data.field : ''),
    })

    // console.log(this.headerPolicy)
    this.fieldExp = (this.headerPolicy=='0' || this.form.get("field")?.value=='') ? '' : `${this.form.get("field")?.value}: [${this.toolService.maskStringByPolicy(this.form.get("field")?.value,this.headerPolicy,this.headerPolicyNum,this.headerPolicyMask)}]`;

    this.form.valueChanges.subscribe((res: { field: string, no: number }) => {
      this.fieldExp = `${res.field}: [${this.toolService.maskStringByPolicy(res.field,this.headerPolicy,this.headerPolicyNum,this.headerPolicyMask)}]`;
      this.change.emit({ field: res.field, no: this.no! });
    });
  }



  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }



}




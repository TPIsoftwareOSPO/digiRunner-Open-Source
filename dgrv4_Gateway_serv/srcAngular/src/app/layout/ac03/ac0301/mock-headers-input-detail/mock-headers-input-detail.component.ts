import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-mock-headers-input-detail',
  templateUrl: './mock-headers-input-detail.component.html',
  styleUrls: ['./mock-headers-input-detail.component.css']
})
export class MockHeadersInputDetailComponent implements OnInit {

  @Input() data?: { key: string, value: string, no: number };
  @Input() _ref: any;
  @Input() no?: number;

  @Output() change: EventEmitter<{key: string, value: string, no: number}> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder
  ) { }


  ngOnInit(): void {
    this.form = this.fb.group({
      key: new FormControl(this.data ? this.data.key : ''),
      value: new FormControl(this.data ? this.data.value : ''),
    })

    this.form.valueChanges.subscribe((res: {key: string, value: string, no: number}) => {
      this.change.emit({ key: res.key, value: res.value, no: this.no! });
    });
  }

  deleteRow() {
    this._ref.destroy();
    this.remove.emit(this.no);
  }
}

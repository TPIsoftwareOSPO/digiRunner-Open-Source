import { FormBuilder, FormGroup, FormControl } from '@angular/forms';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';


@Component({
  selector: 'app-src-url-input-detail',
  templateUrl: './src-url-input-detail.component.html',
  styleUrls: ['./src-url-input-detail.component.css']
})
export class SrcUrlInputDetailComponent implements OnInit {

  @Input() data?: { percent: string, url: string, no: number };
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;

  @Output() change: EventEmitter<{percent: string, url: string, no: number}> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder
  ) { }


  ngOnInit(): void {
    this.form = this.fb.group({
      percent: new FormControl(this.data ? this.data.percent : '',),
      url: new FormControl(this.data ? this.data.url : ''),
    })

    this.form.valueChanges.subscribe((res: {percent: string, url: string, no: number}) => {
      this.change.emit({ percent: res.percent, url: res.url, no: this.no! });
    });
  }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  public get percent() { return this.form.get('percent'); };
  public get url() { return this.form.get('url'); };

}

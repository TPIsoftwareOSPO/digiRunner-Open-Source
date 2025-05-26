import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-src-url-reg-detail',
  templateUrl: './src-url-reg-detail.component.html',
  styleUrls: ['./src-url-reg-detail.component.css']
})
export class SrcUrlRegDetailComponent implements OnInit {

  @Input() data?: { percent: string, url: string, no: number };
  @Input() _ref: any;
  @Input() no?: number;
  @Input() disabled: boolean = false;

  @Output() change: EventEmitter<{percent: string, url: string, no: number}> = new EventEmitter;
  @Output() remove: EventEmitter<number> = new EventEmitter;
  @Output() testApiEvt: EventEmitter<string> = new EventEmitter;

  form!: FormGroup;

  constructor(
    private fb: FormBuilder
  ) { }

  ngOnInit(): void {
    this.form = this.fb.group({
      percent: new FormControl(this.data ? this.data.percent : 0),
      url: new FormControl(this.data ? this.data.url : ''),
    })

    if(this.disabled){
      this.form.disable()
    }else{
      this.form.enable();
    }



    this.form.valueChanges.subscribe((res: {percent: string, url: string, no: number}) => {
      this.change.emit({ percent: res.percent, url: res.url, no: this.no! });
    });
  }

  delete($event) {
    this._ref.destroy();
    this.remove.emit(this.no);
  }

  testApi(){
    this.testApiEvt.emit(this.url.value);
  }

  public get percent() { return this.form.get('percent'); };
  public get url() { return this.form.get('url')!; };

}

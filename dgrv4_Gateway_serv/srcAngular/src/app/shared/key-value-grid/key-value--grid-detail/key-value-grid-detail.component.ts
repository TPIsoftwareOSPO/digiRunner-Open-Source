import { MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { IKeyValueGrid } from './../key-value-grid.interface';
import { Component, OnInit, Input, ComponentRef, AfterViewInit, SimpleChange, EventEmitter, Output, ChangeDetectionStrategy } from '@angular/core';
import { NgModel, FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../validator-functions';


@Component({
  selector: 'app-key-value-grid-detail',
  templateUrl: './key-value-grid-detail.component.html',
  styleUrls: ['./key-value-grid-detail.component.css'],
  changeDetection:ChangeDetectionStrategy.OnPush
})
export class KeyValueGridDetailComponent implements OnInit,AfterViewInit {
  changeLog = [];
  keyvalue :IKeyValueGrid |undefined;
  form:FormGroup;

  ngAfterViewInit(): void {

  }
  @Input() data?:{key:string,value:any,no?:any};
  @Input() _ref :any;
  @Input() no:number|undefined;
  @Output() remove : EventEmitter<number>= new EventEmitter;
  @Output() change : EventEmitter<IKeyValueGrid>= new EventEmitter;
  keyLabel:string|undefined;
  valueLabel:string|undefined;


  valueTypeList : {label:string,value:string}[] = [
    {label:'TEXT',value:'text'},
    {label:'FILE',value:'file'}
  ]

  _fileName:string = ''



  constructor(
    private fb:FormBuilder,
    private toolService: ToolService,
    private messageService:MessageService
    ) {
    this.form = this.fb.group({
      selected:new FormControl(true),
      key : new FormControl(this.data ? this.data.key : '',[ValidatorFns.maxLengthValidator(30)]),
      value : new FormControl(this.data ? this.data.value : ''),
      valueType : new FormControl('text'),
      file: [null],
    })
  }

  ngOnInit() {
    // this.form = this.fb.group({
    //   key : new FormControl(this.data ? this.data.key : '',[ValidatorFns.maxLengthValidator(30)]),
    //   value : new FormControl(this.data ? this.data.value : '')
    // })
    this.keyvalue = {} as IKeyValueGrid;
    this.form.valueChanges.subscribe((res:{key:string,value:any,selected:boolean, file:File, valueType:string}) => {
      if(res.valueType == 'file')
      {
        this.change.emit({key : res.key , value : res.value , no : this.no, selected: res.selected, file:res.file} as IKeyValueGrid);
      }
      else
      {
        this.change.emit({key : res.key , value : res.value , no : this.no, selected: res.selected} as IKeyValueGrid);
      }
    })

    this.valueType.valueChanges.subscribe(()=>{
      this.removeFile();
    })

  }
  delete($event:any){
    this._ref.destroy();
    this.remove.emit(this.no);
  }



  onFileChange(event) {
    // let reader = new FileReader();

    if(event.target.files && event.target.files.length) {
      const _file = event.target.files[0];

      this.form.patchValue({
        file: _file
      });
      this._fileName = _file.name;
    }
  }

  openFileBrowser() {
    $('#fileField'+this.no).click();
  }

  removeFile(){
    this.file.setValue(null);
    this._fileName = '';
    $('#fileField'+this.no).val('');
  }


  public get key() { return this.form.get('key')!; };
  public get value() { return this.form.get('value')!; };
  public get selected() { return this.form.get('selected')!; };
  public get valueType() { return this.form.get('valueType')!; };
  public get file() { return this.form.get('file')!; };


}

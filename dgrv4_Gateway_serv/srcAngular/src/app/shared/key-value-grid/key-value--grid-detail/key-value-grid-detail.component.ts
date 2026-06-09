import { MessageService } from 'primeng/api';
import { ToolService } from 'src/app/shared/services/tool.service';
import { IKeyValueGrid } from './../key-value-grid.interface';
import { Component, OnInit, Input, ComponentRef, AfterViewInit, SimpleChange, EventEmitter, Output, ChangeDetectionStrategy } from '@angular/core';
import { NgModel, UntypedFormGroup, UntypedFormBuilder, UntypedFormControl } from '@angular/forms';
import * as ValidatorFns from '../../validator-functions';


@Component({
    selector: 'app-key-value-grid-detail',
    templateUrl: './key-value-grid-detail.component.html',
    styleUrls: ['./key-value-grid-detail.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    standalone: false
})
export class KeyValueGridDetailComponent implements OnInit,AfterViewInit {
  changeLog = [];
  keyvalue :IKeyValueGrid |undefined;
  form:UntypedFormGroup;

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

  isMima: boolean = false;
  useMask: boolean = false;

  constructor(
    private fb:UntypedFormBuilder,
    private toolService: ToolService,
    private messageService:MessageService
    ) {
    this.form = this.fb.group({
      selected:new UntypedFormControl(true),
      key : new UntypedFormControl(this.data ? this.data.key : '',[ValidatorFns.maxLengthValidator(30)]),
      value : new UntypedFormControl(this.data ? this.data.value : ''),
      valueType : new UntypedFormControl('text'),
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
    this.isMima = this.key?.value.toLowerCase().startsWith('auth') || this.key?.value.toLowerCase().startsWith('x-api-key');
    this.key?.valueChanges.subscribe((key) => {
      if (
        key.toLowerCase().startsWith('auth') ||
        key.toLowerCase().startsWith('x-api-key')
      ) {
        this.isMima = true;
      } else {
        this.isMima = false;
      }
    });

  }
  delete($event:any){
    this._ref.destroy();
    this.remove.emit(this.no);
  }



  onFileChange(event) {

    if(event.target.files && event.target.files.length) {

      const files: FileList = event.target.files;

      if (files && files.length>0) {
        const currentFiles: File[] = this.form.get('file')?.value || [];
        const updatedFiles: File[] = [...currentFiles, ...Array.from(files)];
        this.form.patchValue({
          file: updatedFiles
        });
        this._fileName = updatedFiles.map(f => f.name).join(', ');
        event.target.value = '';
      }
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

import { Component, Input, OnInit, ViewChild, ViewContainerRef, forwardRef,SimpleChanges } from '@angular/core';
import { FieldsFormDetailComponent } from '../fields-form-detail/fields-form-detail.component';
import { NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'app-fields-form',
  templateUrl: './fields-form.component.html',
  styleUrls: ['./fields-form.component.css'],
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => FieldsFormComponent),
    multi: true
  }]
})
export class FieldsFormComponent implements OnInit {

  onTouched!: () => void;
  onChange!: (value: any) => void;

  @ViewChild('fields', { read: ViewContainerRef, static: true }) fieldsRef!: ViewContainerRef;

  disabled: boolean = false;
  no: number = 0;
  fieldList: { field: string, no: number }[] = [];

  @Input() headerPolicy: string = '0';
  @Input() headerPolicyNum: number = 0;
  @Input() headerPolicyMask: string = '*';

  constructor() { }



  ngOnInit(): void {

  }

  ngOnChanges(changes: SimpleChanges): void {
    //Called before any other lifecycle hook. Use it to inject dependencies, but avoid any serious work here.
    //Add '${implements OnChanges}' to the class.
    if(changes['headerPolicy'] || changes['headerPolicyNum'] || changes['headerPolicyMask'] ){
      // console.log(changes['headerPolicy'],changes['headerPolicyNum'],changes['headerPolicyMask'])
      // console.log(this.headerPolicy);
      // console.log(this.headerPolicyNum);
      // console.log(this.headerPolicyMask);
      // console.log(this.fieldList)

      if(changes['headerPolicy']) this.headerPolicy = changes['headerPolicy'].currentValue;
      if(changes['headerPolicyNum']) this.headerPolicyNum = changes['headerPolicyNum'].currentValue;
      if(changes['headerPolicyMask']) this.headerPolicyMask = changes['headerPolicyMask'].currentValue;
      this.refactFielsByChange();
    }

  }

  refactFielsByChange(){
    this.fieldsRef.clear();

    this.fieldList.forEach((item,index)=>{

      let componentRef = this.fieldsRef.createComponent(FieldsFormDetailComponent);
      componentRef.instance._ref = componentRef;
      componentRef.instance.no = item.no;
      componentRef.instance.data = this.fieldList[index];
      componentRef.instance.headerPolicy = this.headerPolicy;
      componentRef.instance.headerPolicyNum = this.headerPolicyNum;
      componentRef.instance.headerPolicyMask = this.headerPolicyMask;



      componentRef.instance.change.subscribe((res: { field: string, no: number }) => {
        let idx = this.fieldList.findIndex(x => x.no === res.no);

        this.fieldList[idx].field = res.field;
        this.fieldList[idx].no = res.no;


        this.onChange(this.formatFieldsToString(this.fieldList))
      })

      componentRef.instance.remove.subscribe(no => {
        let idx = this.fieldList.findIndex(x => x.no === no);
        this.fieldList.splice(idx, 1);
        if (this.fieldList.length == 0) {
          this.no = 0;
          this.onChange(undefined);
          this.addFields();
        }else{
          this.onChange(this.formatFieldsToString(this.fieldList))
        }
      })

    })
  }

  addFields(headerMaskKey?: string) {
    let componentRef = this.fieldsRef.createComponent(FieldsFormDetailComponent);


    if(headerMaskKey){
      this.fieldList.push({field:headerMaskKey, no: this.no})
    }
    else{
      this.fieldList.push({field:'', no: this.no})
    }

    componentRef.instance._ref = componentRef;
    componentRef.instance.no = this.no;
    componentRef.instance.data = this.fieldList[this.no];
    componentRef.instance.headerPolicy = this.headerPolicy;
    componentRef.instance.headerPolicyNum = this.headerPolicyNum;
    componentRef.instance.headerPolicyMask = this.headerPolicyMask;
    this.no++;


    if(this.onChange) this.onChange(this.formatFieldsToString(this.fieldList))

    componentRef.instance.change.subscribe((res: { field: string, no: number }) => {
      let idx = this.fieldList.findIndex(x => x.no === res.no);

      this.fieldList[idx].field = res.field;
      this.fieldList[idx].no = res.no;


      this.onChange(this.formatFieldsToString(this.fieldList))
    })

    componentRef.instance.remove.subscribe(no => {
      let idx = this.fieldList.findIndex(x => x.no === no);
      this.fieldList.splice(idx, 1);
      if (this.fieldList.length == 0) {
        this.no = 0;
        this.onChange(undefined);
        this.addFields();
      }else{
        this.onChange(this.formatFieldsToString(this.fieldList))
      }
    })

  }

  formatFieldsToString(data?:{ field: string, no: number }[]){
    return data? data.map(data=> data.field.trim()).filter(val=> val && val.trim() != '').join(','): ''
  }

  writeValue(headerMaskKey?: string) {
    // console.log('write value')
    this.initFields(headerMaskKey);
  }

  initFields(headerMaskKey?: string) {
    this.no = 0;
    this.fieldList = [];
    this.fieldsRef.clear();

    if(headerMaskKey && headerMaskKey.trim() != ''){
      headerMaskKey?.split(',').forEach(item=>{

        this.addFields(item);
      })
    }
    else{
      this.addFields();
    }

  }

  registerOnChange(fn: (value: any) => void): void {
    this.onChange = fn
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    // console.log('接收disabled狀態', isDisabled)
    this.disabled = isDisabled;

    // this.generateSrcUrlReg();
  }

}

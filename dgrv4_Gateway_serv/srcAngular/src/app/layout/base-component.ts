import { ValidatorFormat } from './../models/validator.interface';
// import { TransformMenuNamePipe } from './../shared/pipes/transform-menu-name.pipe';
import { OnInit, Injectable } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { FormGroup, FormArray } from '@angular/forms';
import { TransformMenuNamePipe } from '../shared/pipes/transform-menu-name.pipe';
// import { ValidatorFormat } from '../models/validator.interface';
import * as ValidatorFns from '../shared/validator-functions';

export class BaseComponent {

    title: string = '';

    constructor(
        protected route: ActivatedRoute,
        protected tr: TransformMenuNamePipe
    ) {
      this.initTitle();

    }

    initTitle(){
      if(this.route.snapshot.params['cusfunc']){
        let id = this.route.snapshot.params['cusfunc'];
        this.title = this.tr.transform(id.toUpperCase())
      }else{
        this.route.data.subscribe(r => {
          if (r['id']) this.title = this.tr.transform(r['id'].toUpperCase())
      });
      }

    }

    public getControls(frmGrp: FormGroup, key: string) {
        return (<FormArray>frmGrp.controls[key]).controls;
    }

    public compare(a: number | string, b: number | string, isAsc: boolean) {
        return (a < b ? -1 : 1) * (isAsc ? 1 : -1);
    }

    public addFormValidator(form: FormGroup, validators: Array<ValidatorFormat>) {
        try {
            validators.map(item => {
                let _validator  = new Array;
                if (item.isRequired) {
                    _validator.push(ValidatorFns.isRequiredValidator(item.isRequired.msg));
                    $(`#${item.field}_label`).addClass('required');
                }
                if (item.maxLength) {
                    _validator.push(ValidatorFns.maxLengthValidator(item.maxLength.value, item.maxLength.msg));
                }
                if (item.max) {
                    _validator.push(ValidatorFns.maxValidator(item.type, item.max.value, item.max.msg));
                }
                if (item.minLength) {
                    _validator.push(ValidatorFns.minLengthValidator(item.minLength.value, item.minLength.msg));
                }
                if (item.min) {
                    _validator.push(ValidatorFns.minValidator(item.type, item.min.value, item.min.msg));
                }
                if (item.pattern) {
                    _validator.push(ValidatorFns.patternValidator(item.pattern.value, item.pattern.msg));
                }
                form.controls[item.field].setValidators(_validator);
                form.controls[item.field].updateValueAndValidity();
            });
        }
        catch (e) { // field沒有相對應的formControlName須重置form的所有vlaidator
            // console.log('form validator error :', e)
            this.resetFormValidator(form);
        }
    }

    public resetFormValidator(form: FormGroup,formReset = true) {
        for (const formControlName in form.controls) {
            $(`#${formControlName}_label`).removeClass('required');
            if (formReset) form.controls[formControlName].reset('')
            form.controls[formControlName].clearValidators();
            form.controls[formControlName].updateValueAndValidity();
        }
    }

}

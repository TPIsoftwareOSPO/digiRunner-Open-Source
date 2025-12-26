import {
  ValidatorFn,
  AbstractControl,
  FormGroup,
  ValidationErrors,
} from '@angular/forms';
// let ipRegex = require('ip-regex');

export function requiredValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    const t = Object.prototype.toString.call(value);
    if (t === '[object Array]') {
      if (!value.length) {
        return { required: 'validation.required' };
      }
    }
    let obj = { required: 'validation.required' };
    return value === '' || value === undefined || value === null ? obj : null;
  };
}

export function isRequiredValidator(msg: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    const t = Object.prototype.toString.call(value);
    if (t === '[object Array]') {
      if (!value.length) {
        return { isRequired: msg };
      }
    }
    let obj = { isRequired: msg };
    return value === '' || value === undefined ? obj : null;
  };
}

export function stringCharValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-\s0-9\@\.]+/);
    if (pa.test(value))
      return pa.test(value) ? { stringchart: 'validation.stringchart' } : null;
    return null;
  };
}

export function stringCodeValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-0-9\.]+/);
    if (value && value.length > maxLength)
      return { maxlength: 'validation.maxlength' };
    if (pa.test(value))
      return pa.test(value) ? { stringcode: 'validation.stringcode' } : null;
    return null;
  };
}

export function stringNameValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-0-9]+/);
    if (value && value.length > maxLength)
      return { maxlength: 'validation.maxlength' };
    if (pa.test(value))
      return pa.test(value) ? { stringname: 'validation.stringname' } : null;
    return null;
  };
}

export function stringNameSpaceValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-0-9\ ]+/);
    if (value && value.length > maxLength)
      return { maxlength: 'validation.maxlength' };
    if (pa.test(value))
      return pa.test(value)
        ? { stringname: 'validation.stringname_space' }
        : null;
    return null;
  };
}

export function stringAliasValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-\s0-9\u4E00-\u9fa5]+/);
    // for (let i = 0; i < value.length; i++) {
    //     let cahrCode = value.charCodeAt(i);
    //     // if (cahrCode > 255) { // 中文字 ASCII > 255
    //     //     _len += 2;
    //     // }
    //     // else {
    //     //     _len++;
    //     // }
    //     while (cahrCode > 0) {
    //         _len++;
    //         cahrCode = cahrCode >> 8; // 一個中文 2 Bytes -> 8 bit
    //     }
    // }
    // _len = encodeURIComponent(value).replace(/%[A-F\d]{2}/g, 'U').length; // UTF-8 length;
    if (value) {
      let _char = value.match(/[^\x00-\xff]/gi);
      let _len = 0;
      _char == null
        ? (_len = value.length)
        : (_len = value.length + _char.length);
      if (_len > maxLength) return { maxlength: 'validation.aliasmaxlength' };
    }
    if (pa.test(value))
      return pa.test(value) ? { stringalias: 'validation.stringalias' } : null;
    return null;
  };
}

export function stringSpaceAliasValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z\_\-\s0-9\u4E00-\u9fa5]+/);
    // for (let i = 0; i < value.length; i++) {
    //     let cahrCode = value.charCodeAt(i);
    //     // if (cahrCode > 255) { // 中文字 ASCII > 255
    //     //     _len += 2;
    //     // }
    //     // else {
    //     //     _len++;
    //     // }
    //     while (cahrCode > 0) {
    //         _len++;
    //         cahrCode = cahrCode >> 8; // 一個中文 2 Bytes -> 8 bit
    //     }
    // }
    // _len = encodeURIComponent(value).replace(/%[A-F\d]{2}/g, 'U').length; // UTF-8 length;
    if (value) {
      let _char = value.match(/[^\x00-\xff]/gi);
      let _len = 0;
      _char == null
        ? (_len = value.length)
        : (_len = value.length + _char.length);
      if (_len > maxLength) return { maxlength: 'validation.aliasmaxlength' };
    }
    if (pa.test(value))
      return pa.test(value)
        ? { stringalias: 'validation.stringalias_space' }
        : null;
    return null;
  };
}

export function stringANumValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(/[^a-zA-Z0-9]+/);
    if (value && value.length > maxLength)
      return { maxlength: 'validation.maxlength' };
    if (pa.test(value))
      return pa.test(value) ? { stringanum: 'validation.stringanum' } : null;
    return null;
  };
}

export function mailValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    const regex = new RegExp(/[a-zA-Z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$/g);
    const result = regex.test(value);
    if (!result && value != '') return { mailFormat: 'validation.mailFormat' };
    return null;
  };
}

export function websiteAddressValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    // const regex = new RegExp(/[^?:http(s)?:\/\/][\w\W]+/g);
    const regex = new RegExp(/^http(s)?:\/\/.+/g);
    const result = regex.test(value);
    if (!result && value != '')
      return { websiteAddress: 'validation.websiteAddress' };
    return null;
  };
}

export function maxLengthValidator(
  maxLength: number,
  msg?: string
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    // if (value) {
    //     let _char = value.match(/[^\x00-\xff]/ig);
    //     let _len = 0;
    //     _char == null ? _len = value.length : _len = value.length + _char.length;
    //     if (_len > maxLength) return { maxlength: msg ? msg : 'validation.maxlength' }
    // }
    if (value && value.length > maxLength)
      return { maxlength: msg ? msg : 'validation.maxlength' };
    // if (value && value.length > maxLength) return { maxlength: 'validation.maxlength' }
    return null;
  };
}

export function maxValidator(
  dataType: string,
  max: number,
  msg: string
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value) {
      switch (dataType) {
        case 'string':
          if (value.length > max) return { max: msg };
          break;
        case 'int':
          if (Number(value) > max) return { max: msg };
          break;
        default:
          return { max: null };
      }
    }
    return null;
  };
}

export function minLengthValidator(
  minLength: number,
  msg?: string
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value && value.length < minLength)
      return { minlength: msg ? msg : 'validation.minlength' };
    return null;
  };
}

export function minValidator(
  dataType: string,
  min: number,
  msg: string
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value) {
      switch (dataType) {
        case 'string':
          if (value.length < min) return { min: msg };
          break;
        case 'int':
          if (Number(value) < min) return { min: msg };
          break;
        default:
          return { max: null };
      }
    }
    return null;
  };
}

export function stringLengthValidator(maxLength: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    if (value && value.length > maxLength)
      return { maxlength: 'validation.maxlength' };
    return null;
  };
}

export function ipAddressValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value;
    const regex = new RegExp(
      /^(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))\.(\d|[1-9]\d|1\d\d|2([0-4]\d|5[0-5]))$/g
    );
    const result = regex.test(value);
    if (!result) return { ipaddress: 'validation.ipaddress' };
    return null;
  };
}

export function confirmPasswordForUserValidator(
  group: FormGroup,
  useUpdate: boolean = false
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    let oldPwdControl = useUpdate
      ? group.controls['newUserBlock']
      : group.controls['userBlock'];
    let newPwdControl = group.controls['confirmUserBlock'];

    let oldPwd = oldPwdControl.value;
    let newPwd = newPwdControl.value;
    const errorKey = 'confirm_mima';

    if (oldPwd === newPwd) {
      if (oldPwdControl.hasError(errorKey)) {
        const errors = oldPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          oldPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      if (newPwdControl.hasError(errorKey)) {
        const errors = newPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          newPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      return null;
    } else {
      return { confirm_mima: 'validation.confirm_mima' };
    }
  };
}

export function confirmPasswordForClientValidator(
  group: FormGroup,
  useUpdate: boolean = false
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    let oldPwdControl = useUpdate
      ? group.controls['newClientBlock']
      : group.controls['clientBlock'];
    let newPwdControl = group.controls['confirmClientBlock'];

    let oldPwd = oldPwdControl.value;
    let newPwd = newPwdControl.value;

    const errorKey = 'confirm_mima';

    if (oldPwd === newPwd) {
      if (oldPwdControl.hasError(errorKey)) {
        const errors = oldPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          oldPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      if (newPwdControl.hasError(errorKey)) {
        const errors = newPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          newPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      return null;
    } else {
      return { confirm_mima: 'validation.confirm_mima' };
    }
  };
}

export function confirmPasswordValidator(
  group: FormGroup,
  oriField: string = '',
  newField: string = ''
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!group.controls[oriField] || !group.controls[newField]) {
      return null;
    }

    let oldPwdControl = group.controls[oriField];
    let newPwdControl = group.controls[newField];

    let oldPwd = oldPwdControl.value;
    let newPwd = newPwdControl.value;

    const errorKey = 'confirm_mima';

    if (oldPwd === newPwd) {
      if (oldPwdControl.hasError(errorKey)) {
        const errors = oldPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          oldPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      if (newPwdControl.hasError(errorKey)) {
        const errors = newPwdControl.errors;
        if (errors) {
          delete errors[errorKey];
          newPwdControl.setErrors(Object.keys(errors).length ? errors : null);
        }
      }
      return null;
    } else {
      return { confirm_mima: 'validation.confirm_mima' };
    }
  };
}

export function patternValidator(pattern: string, msg: string): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = control.value as string;
    const pa: RegExp = new RegExp(pattern);
    try {
      if (value && value.match(pa)) {
        return value.match(pa)![0] != control.value ? { pattern: msg } : null;
      } else if (value && !value.match(pa)) {
        return { pattern: msg };
      }
    } catch (error) {
      return null;
    }
    return null;
    // if (value && value.match(pa)) {
    //     return value.match(pa)[0] != control.value ? { pattern: msg } : null;
    // }
    // else if (value && !value.match(pa)) {
    //     return { pattern: msg };
    // }
    // if (pa.test(value)) return pa.test(value) ? { pattern: msg } : null;
    // if (pa.test(value) == false) {
    //     return { pattern: msg };
    // }
  };
}

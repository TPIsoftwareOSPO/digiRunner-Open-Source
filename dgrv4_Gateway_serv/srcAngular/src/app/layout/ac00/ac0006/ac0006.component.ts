import { LogoutService } from './../../../shared/services/logout.service';
import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormControl, FormGroupDirective } from '@angular/forms';
import { UserService } from '../../../shared/services/api-user.service';
import * as ValidatorFns from '../../../shared/validator-functions';
import { ToolService } from '../../../shared/services/tool.service';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { AA0006Req } from 'src/app/models/api/UserService/aa0006.interface';
import { AlertService } from 'src/app/shared/services/alert.service';
import { AA0003Req, AA0003Resp } from 'src/app/models/api/UserService/aa0003.interface';
import { MessageService } from 'primeng/api';
import * as base64 from 'js-base64'
import * as dayjs from 'dayjs';

@Component({
    selector: 'app-ac0006',
    templateUrl: './ac0006.component.html',
    styleUrls: ['./ac0006.component.css'],
    providers: [UserService]
})
export class Ac0006Component extends BaseComponent implements OnInit {

    form: FormGroup;
    // isValid: boolean;
    user_id: string = '';
    user_name: string = '';
    newUserNameLimitChar = { value: 30 };
    newUserAliasLimitChar = { value: 30 }
    newUserBlockLimitChar = { value: 128 };
    newUserMailLimitChar = { value: 50 };
    userDetail?: AA0003Resp;

    idTokenJwtstr:string= sessionStorage.getItem('idTokenJwtstr')?? '';
    tokenData:{[key:string]:any}[] = [];
    isidTokenForm:boolean = false;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private userService: UserService,
        private tool: ToolService,
        private alert: AlertService,
        private router: Router,
        private message: MessageService,
        private logoutService:LogoutService
    ) {
        super(route, tr);
        this.form = this.fb.group({
            newUserName: new FormControl(''),//, [ValidatorFns.stringNameValidator(this.newUserNameLimitChar.value)]),
            newUserAlias: new FormControl(''),//, [ValidatorFns.stringAliasValidator(this.newUserAliasLimitChar.value)]),
            newUserMail: new FormControl(''),//, [ValidatorFns.maxLengthValidator(this.newUserMailLimitChar.value)]),
            userBlock: new FormControl('', [ValidatorFns.maxLengthValidator(this.newUserBlockLimitChar.value)]),
            newUserBlock: new FormControl(''),
            confirmUserBlock: new FormControl(''),
        });
        // this.form.controls.newUserBlock.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form, true), ValidatorFns.maxLengthValidator(this.newUserBlockLimitChar.value)]);
        // this.form.controls.confirmUserBlock.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form, true), ValidatorFns.maxLengthValidator(this.newUserBlockLimitChar.value)]);
        this.form.get('newUserBlock')?.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form, true), ValidatorFns.maxLengthValidator(this.newUserBlockLimitChar.value)]);
        this.form.get('confirmUserBlock')?.setValidators([ValidatorFns.confirmPasswordForUserValidator(this.form, true), ValidatorFns.maxLengthValidator(this.newUserBlockLimitChar.value)]);
    }

    ngOnInit() {
        // this.form.valueChanges.subscribe(r => this.isValid = (this.form.status === 'VALID'));
        this.user_id = this.tool.getUserID();
        this.user_name = this.tool.getUserName();
        this.queryUserDetail();

        this.userService.updateTUserData_before().subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
            }
        });

        if(this.idTokenJwtstr!='')
        {
          const tmpTokenPool:string[] =  this.idTokenJwtstr.split('.');
          // console.log(this.tool.base64_decode(tmpTokenPool[1]))
          const strToken:string = tmpTokenPool.length>1 ? base64.Base64.decode(tmpTokenPool[1]) : '';

          if(strToken!='')
          {
            const jsonToken = JSON.parse(strToken);
            if(jsonToken){
              this.isidTokenForm = true;
              Object.keys(jsonToken).map(key => {
                this.tokenData.push({'label': key , 'value':jsonToken[key] } )
              })
            }
          }
        }
    }

    queryUserDetail() {
        let ReqBody = {
            userID: this.user_id,
            userName: this.user_name
        } as AA0003Req;
        this.userService.queryTUserDetail(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.userDetail = res.RespBody;
                this.newUserName!.setValue(this.userDetail.userName);
                this.newUserAlias!.setValue(this.userDetail.userAlias);
                this.newUserMail!.setValue(this.userDetail.userMail);
            }
        });
    }

    submitForm(formDirective: FormGroupDirective) {
        //預埋api
        let ReqBody = {
            userId: this.userDetail!.userID,
            userName: this.userDetail!.userName,
            newUserName: this.newUserName!.value,
            userAlias: this.userDetail!.userAlias,
            newUserAlias: this.newUserAlias!.value,
            userMail: this.userDetail!.userMail,
            newUserMail: this.newUserMail!.value,
            userBlock: this.tool.Base64Encoder(this.userBlock!.value),
            newUserBlock: this.tool.Base64Encoder(this.newUserBlock!.value)
        } as AA0006Req;
        this.userService.updateTUserData(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const code = ['message.update', 'profile', 'message.success', 'plz_login_again'];
                const dict = await this.tool.getDict(code);
                this.message.add({ severity: 'success', summary: `${dict['message.update']} ${dict['profile']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });;
                if ((ReqBody.newUserName != '' && ReqBody.newUserName != this.user_name) || (ReqBody.newUserBlock != '' && ReqBody.userBlock != ReqBody.newUserBlock)) {
                    this.alert.logout(`${dict['message.update']} ${dict['message.success']}!`, `${dict['plz_login_again']}!`);
                    window.setTimeout(() => {
                        // this.tool.removeAll();
                        // this.router.navigate(['/login']);

                        // this.logoutService.logout();
                        this.tool.setClearExpiredTimeout();
                    }, 3000);
                }
                else {
                    this.queryUserDetail();
                }
            }
        });
    }


  formateDate(date:Date){
    const procDate = Number(date)*1000;
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date'? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss'): '';
  }

    public get newUserName() { return this.form.get('newUserName'); };
    public get newUserAlias() { return this.form.get('newUserAlias'); };
    public get newUserMail() { return this.form.get('newUserMail'); };
    public get userBlock() { return this.form.get('userBlock'); };
    public get newUserBlock() { return this.form.get('newUserBlock'); };
    public get confirmUserBlock() { return this.form.get('confirmUserBlock'); };

}

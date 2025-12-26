import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { AA1212BadAttemptItemResp } from 'src/app/models/api/ReportService/aa1212.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { UserService } from '../services/api-user.service';
import { BaseComponent } from 'src/app/layout/base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from '../pipes/transform-menu-name.pipe';
import * as ValidatorFns from '../../shared/validator-functions';
import { MessageService } from 'primeng/api';

@Component({
  selector: 'app-first-time-login',
  templateUrl: './first-time-login.component.html',
  styleUrls: ['./first-time-login.component.css'],
})
export class FirstTimeLoginComponent extends BaseComponent implements OnInit {
  form!: FormGroup;

  oriMask: boolean = false;
  newMask: boolean = false;
  cfmMask: boolean = false;

  mimatip?: string;
  mimaPattern?: string;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private ngxSrvice: NgxUiLoaderService,
    private userService: UserService,
    private ref: DynamicDialogRef
  ) {
    super(route, tr);
  }

  ngOnInit() {
    this.form = this.fb.group({
      oriMima: new FormControl(''),
      newMima: new FormControl(''),
      confirmNewMima: new FormControl(''),
    });

    this.userService.updateNewMima_before().subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.addFormValidator(this.form, res.RespBody.constraints);
      }

      this.userService.getMimaStrengthDesc().subscribe((res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.mimatip = res.RespBody.acPwdStrengthDesc;
          this.mimaPattern = res.RespBody.acPwdStrength;
          if (this.mimaPattern) {
            this.newMima?.addValidators([
              ValidatorFns.patternValidator(
                this.mimaPattern,
                this.mimatip ?? this.mimaPattern
              ),
            ]);
          }
        }
      });

      this.form
        .get('newMima')
        ?.addValidators([
          ValidatorFns.confirmPasswordValidator(
            this.form,
            'newMima',
            'confirmNewMima'
          ),
        ]);
      this.form
        .get('confirmNewMima')
        ?.addValidators([
          ValidatorFns.confirmPasswordValidator(
            this.form,
            'newMima',
            'confirmNewMima'
          ),
        ]);
    });
  }

  confirm() {
    this.ngxSrvice.start();
    this.userService
      .updateNewMima({
        oriMima: this.toolService.Base64Encoder(this.oriMima!.value),
        newMima: this.toolService.Base64Encoder(this.newMima!.value),
      })
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.ref.close();
        }
        this.ngxSrvice.stop();
      });
  }

  toggleMask(tar: string) {
    switch (tar) {
      case 'oriMima':
        this.oriMask = !this.oriMask;
        break;
      case 'newMima':
        this.newMask = !this.newMask;
        break;
      case 'cfmMima':
        this.cfmMask = !this.cfmMask;
        break;

      default:
        break;
    }
  }

  public get oriMima() {
    return this.form.get('oriMima');
  }
  public get newMima() {
    return this.form.get('newMima');
  }
  public get confirmNewMima() {
    return this.form.get('confirmNewMima');
  }
}

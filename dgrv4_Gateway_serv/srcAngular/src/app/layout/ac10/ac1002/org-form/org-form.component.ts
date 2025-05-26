import { DynamicDialogConfig, DynamicDialogRef } from 'primeng/dynamicdialog';
import { DialogService } from 'primeng/dynamicdialog';
import { Component, OnInit, Input } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import * as ValidatorFns from '../../../../shared/validator-functions';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { FormOperate } from '../../../../models/common.enum';
import { TOrgService } from 'src/app/shared/services/org.service';
import { AA1002List } from 'src/app/models/api/OrgService/aa1002.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { AA1001Req } from 'src/app/models/api/OrgService/aa1001.interface';
import { AA1003Req } from 'src/app/models/api/OrgService/aa1003.interface';
import { MessageService } from 'primeng/api';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { TranslateService } from '@ngx-translate/core';
import { OrganizationComponent } from 'src/app/shared/organization/organization.component';

@Component({
  selector: 'app-org-form',
  templateUrl: './org-form.component.html',
  styleUrls: ['./org-form.component.css'],
  providers: [MessageService]
})
export class OrgFormComponent implements OnInit {

  @Input() data?: FormParams;
  @Input() close?: Function;
  // openDynamicDialogRefs: DynamicDialogRef[] = [];


  form: FormGroup;
  formOperate = FormOperate;
  displayOrgChart: boolean = false;
  submitBtnName: string = '';
  orgList?: AA1002List[];
  orgNameLimitChar = { value: 30 };
  conNameLimitChar = { value: 30 };
  orgCodeLimitChar = { value: 100 };
  conTELLimitChar = { value: 50 };
  conMailLimitChar = { value: 100 };
  isValid: boolean = false;

  isManager: boolean = false;

  constructor(
    private fb: FormBuilder,
    private orgService: TOrgService,
    private toolService: ToolService,
    private ngxService: NgxUiLoaderService,
    public config: DynamicDialogConfig,
    private dialogService: DialogService,
    public ref: DynamicDialogRef,
    private translate: TranslateService

  ) {
    this.form = this.fb.group(this.resetFormGroup(this.config.data.operate));
  }

  async ngOnInit() {
    // console.log('data :', this.config.data)

    if(this.config.data.data?.orgId) this.isManager = this.config.data.data.orgId === '100000';
    this.getOrgList();

    this.form.valueChanges.subscribe(r => {
      this.isValid = (this.form.status === 'VALID')
    });
    const codes = ['button.new', 'button.update'];
    const dict = await this.toolService.getDict(codes);
    switch (this.config.data.operate) {
      case FormOperate.create:
        this.submitBtnName = dict['button.new'];
        break;
      case FormOperate.update:
        this.submitBtnName = dict['button.update'];
        this.orgName!.setValue(this.config.data.data.orgName);
        this.parentId!.setValue(this.config.data.data.parentName);
        this.orgCode!.setValue(this.config.data.data.orgCode);
        this.conName!.setValue(this.config.data.data.contactName);
        this.conTEL!.setValue(this.config.data.data.contactTel);
        this.conMail!.setValue(this.config.data.data.contactMail);
        break;
    }
  }

  getOrgList() {
    this.orgService.queryTOrgList({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.orgList = res.RespBody.orgList;
      }
    });
  }

  submitForm() {
    this.ngxService.start();
    switch (this.config.data.operate) {
      case FormOperate.create:
        let createReqBody = {
          orgName: this.orgName!.value,
          orgCode: this.orgCode!.value,
          parentId: this.orgList?.find(org => org.orgName == this.parentId!.value)?.orgID,
          contactName: this.conName!.value,
          contactTel: this.conTEL!.value,
          contactMail: this.conMail!.value
        } as AA1001Req;
        // console.log('req 1001 :', req_1001)
        let createObservable = this.orgService.addTOrg(createReqBody);
        // if (this.close) this.close(createObservable);
        this.ref.close(createObservable);
        break;
      case FormOperate.update:
        let updateReqBody = {
          orgId: this.config.data.data.orgId,
          orgName: this.config.data.data.orgName,
          newOrgName: this.orgName!.value,
          orgCode: this.config.data.data.orgCode,
          newOrgCode: this.orgCode!.value,
          parentId: this.orgList?.find(org => org.orgName == this.config.data.data.parentName)?.orgID,
          newParentId: this.orgList?.find(org => org.orgName == this.parentId!.value)?.orgID,
          contactName: this.config.data.data.contactName,
          newContactName: this.conName!.value,
          contactTel: this.config.data.data.contactTel,
          newContactTel: this.conTEL!.value,
          contactMail: this.config.data.data.contactMail,
          newContactMail: this.conMail!.value
        } as AA1003Req;
        // console.log('1003 :', req_1003)
        let updateObservable = this.orgService.updateTOrgByOrgId(updateReqBody);
        // if (this.close) this.close(updateObservable);

        this.ref.close(updateObservable)
        break;
    }
  }

  getOrgNode(node: AA1002List) {
    // console.log('selected node :', node)
    this.parentId!.setValue(node.orgName);
  }

  cancelOrg() {
    this.parentId!.setValue('');
    this.displayOrgChart = false;
  }

  private resetFormGroup(formOperate?: FormOperate) {
    //初始化
    if (!formOperate) return {
      orgName: '',
      parentId: '',
      orgCode: '',
      conName: '',
      conTEL: '',
      conMail: ''
    };
    switch (formOperate) {
      case FormOperate.create:
        return {
          orgName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.orgNameLimitChar.value)]),
          parentId: new FormControl('', ValidatorFns.requiredValidator()),
          orgCode: new FormControl('', ValidatorFns.stringNameValidator(this.orgCodeLimitChar.value)),
          conName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.conNameLimitChar.value)]),
          conTEL: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.conTELLimitChar.value)]),
          conMail: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.mailValidator(), ValidatorFns.maxLengthValidator(this.conMailLimitChar.value)])
        }
      case FormOperate.update:
        return {
          orgName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.orgNameLimitChar.value)]),
          parentId: new FormControl('', ValidatorFns.requiredValidator()),
          orgCode: new FormControl('', ValidatorFns.stringNameValidator(this.orgCodeLimitChar.value)),
          conName: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.stringAliasValidator(this.conNameLimitChar.value)]),
          conTEL: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.maxLengthValidator(this.conTELLimitChar.value)]),
          conMail: new FormControl('', [ValidatorFns.requiredValidator(), ValidatorFns.mailValidator(), ValidatorFns.maxLengthValidator(this.conMailLimitChar.value)])
        };
      default:
        return {
          orgName: '',
          parentId: '',
          orgCode: '',
          conName: '',
          conTEL: '',
          conMail: ''
        }
    }
  }

  async openOrgDialog() {
    // this.openDynamicDialogRefs = [];
    const codes = ['org_chart'];
    const dict = await this.toolService.getDict(codes);

    const refDialog = this.dialogService.open(OrganizationComponent, {
      header: dict['org_chart'],
      modal: true,
      data: {
        orgList: this.orgList,
        showFooterBtn: true,
        // dyRef:this.openDynamicDialogRefs,
      },
      width: '90vw',
      height: '100vh'
    })

    // this.openDynamicDialogRefs.push(refDialog)

    refDialog.onClose.subscribe(res => {
      if (res) {
        this.parentId!.setValue(res.data.orgName);
      }
      else {
        this.parentId!.setValue('');
      }

    });





    // const ref = this.dialogService.open(OrgFormComponent, {
    //     data: {
    //       operate: FormOperate.create,
    //       displayInDialog: true,
    //     },
    //     header: {{'org_chart' | translate}}
    //     width: '50vw',
    //     height: '100vh'
    //   });

    //   ref.onClose.subscribe(resOb => {
    //     resOb.subscribe(res => {
    //       if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
    //         this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.organization']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
    //         window.setTimeout(() => {
    //           this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
    //             this.router.navigate(['ac10', 'ac1002']);
    //           });
    //         }, 1000);
    //       }
    //     })
    //   })

  }

  managerUpdate() {
    let updateReqBody = {
      orgId: this.config.data.data.orgId,
      orgName: this.config.data.data.orgName,
      newOrgName: this.orgName!.value,

    } as AA1003Req;
    // console.log('1003 :', updateReqBody)
    let updateObservable = this.orgService.updateTOrgByOrgId(updateReqBody);
    this.ref.close(updateObservable)
  }

  public get orgName() { return this.form.get('orgName'); };
  public get orgCode() { return this.form.get('orgCode') };
  public get parentId() { return this.form.get('parentId'); };
  public get conName() { return this.form.get('conName'); };
  public get conTEL() { return this.form.get('conTEL'); };
  public get conMail() { return this.form.get('conMail'); };
}

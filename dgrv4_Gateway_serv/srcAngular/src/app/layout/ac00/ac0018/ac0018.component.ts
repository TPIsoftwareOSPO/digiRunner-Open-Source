import { DPB0161Req } from './../../../models/api/ServerService/dpb0161.interface';
import { AlertService } from './../../../shared/services/alert.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0160Req } from './../../../models/api/ServerService/dpb0160.interface';
import { FormControl } from '@angular/forms';
import { FormBuilder } from '@angular/forms';
import { FormGroup } from '@angular/forms';
import { DomSanitizer } from '@angular/platform-browser';
import { DPB0159Item } from './../../../models/api/ServerService/dpb0159.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TransformMenuNamePipe } from './../../../shared/pipes/transform-menu-name.pipe';
import { ActivatedRoute } from '@angular/router';
import { BaseComponent } from './../../base-component';
import { Component, OnInit } from '@angular/core';
import * as dayjs from 'dayjs';
import * as base64 from 'js-base64'

@Component({
  selector: 'app-ac0018',
  templateUrl: './ac0018.component.html',
  styleUrls: ['./ac0018.component.css'],
  providers: [ConfirmationService]
})
export class Ac0018Component extends BaseComponent implements OnInit {


  currentTitle: string = this.title;
  pageNum: number = 1;
  cols: { field: string; header: string; }[] = [];
  dataList: Array<DPB0159Item> = [];
  form!: FormGroup;

  // _fileData?: File | null;
  // _fileName: string | null = null;
  _fileSrc: any = null;
  currentAction: string = '';
  currentRowData?: DPB0159Item;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private toolService: ToolService,
    private serverService: ServerService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    private confirmationService: ConfirmationService,
  ) {
    super(route, tr);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      ldapUrl: new FormControl(''),
      ldapBaseDn: new FormControl(''),
      ldapDn: new FormControl(''),
      ldapTimeout: new FormControl(0),
      ldapStatus: new FormControl(),
      approvalResultMail: new FormControl(''),
      iconFile: new FormControl(''),
      pageTitle: new FormControl(''),
    });

    this.queryIdPInfoList_ldap();

  }

  queryIdPInfoList_ldap() {
    this.serverService.queryIdPInfoList_ldap({})
      .subscribe(res => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          this.dataList = res.RespBody.ldapIdPInfoList;
        } else this.dataList = [];
      })
  }

  formateDate(date: Date) {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '';
  }

  headerReturn() {
    this.changePage('query');
  }

  async changePage(action: string, rowData?: DPB0159Item) {

    const code = ['button.delete', 'button.create', 'button.update'];
    const dict = await this.toolService.getDict(code);
    this.currentAction = action;
    this.resetFormValidator(this.form);
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        //  this.userService.addTUser_before().subscribe(res => {
        //   this.addFormValidator(this.form, res.RespBody.constraints);
        // });
        this.clearFile();
        this.serverService.createIdPInfo_ldap_before().subscribe(res => {
          this.addFormValidator(this.form, res.RespBody.constraints);

          this.currentTitle = `${this.title} > ${dict['button.create']}`;
          this.pageNum = 2;
          this.ldapStatus?.markAsTouched();
        })
        break;
      case 'update':
        this.clearFile();
        this.currentRowData = rowData;
        this.serverService.updateIdPInfo_ldap_before().subscribe(res => {
          this.addFormValidator(this.form, res.RespBody.constraints);

          this.currentTitle = `${this.title} > ${dict['button.update']}`;
          this.pageNum = 2;
          // console.log(rowData)
          this.ldapBaseDn?.setValue(rowData?.ldapBaseDn)
          this.ldapUrl?.setValue(rowData?.ldapUrl);
          this.ldapDn?.setValue(rowData?.ldapDn);
          this.ldapStatus?.setValue(rowData?.ldapStatus);
          this.ldapTimeout?.setValue(rowData?.ldapTimeout);
          this.approvalResultMail?.setValue(rowData?.approvalResultMail);
          // this.iconFile?.setValue(rowData?.iconFile);
          this.pageTitle?.setValue(rowData?.pageTitle)
          this._fileSrc = rowData?.iconFile;

        })
        break;
      case 'delete':
        this.delete(rowData!);
        break;
    }
  }

  create() {
    let reqBody = {
      ldapUrl: this.ldapUrl?.value,
      ldapBaseDn: this.ldapBaseDn?.value,
      ldapDn: this.ldapDn?.value,
      ldapTimeout: this.ldapTimeout?.value,
      ldapStatus: this.ldapStatus?.value,
      approvalResultMail: this.approvalResultMail?.value,
      pageTitle: this.pageTitle?.value,
    } as DPB0160Req;

    if (this._fileSrc) reqBody.iconFile = this._fileSrc;
    this.serverService.createIdPInfo_ldap(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} AC LDAP IdP User`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.queryIdPInfoList_ldap();
        this.changePage('query');
      }
    })
  }

  openFileBrowser() {
    $('#fileName').click();
  }

  async fileChange(files: FileList) {
    const code = ['uploading', 'cfm_img_format', 'cfm_size', 'message.success', 'upload_result', 'waiting'];
    const dict = await this.toolService.getDict(code);
    if (files.length != 0) {
      // this.messageService.add({ severity: 'success', summary: dict['uploading'], detail: `${dict['waiting']}!` });
      let fileReader = new FileReader();
      fileReader.onloadend = () => {

        if (files.item(0)!.size > 2000) {
          this.alertService.ok('Error', 'File size cannot exceed 2k.');
          this.clearFile();
          return;
        }

        this.messageService.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });

        this._fileSrc = fileReader.result!;


      }
      fileReader.readAsDataURL(files.item(0)!);
    }
    else {
      this._fileSrc = null;
    }
  }

  public clearFile() {
    this._fileSrc = null;
    $('#fileName').val('');
  }

  update() {
    let reqBody = {
      id: this.currentRowData?.id,
      ldapBaseDn: this.ldapBaseDn?.value,
      ldapUrl: this.ldapUrl?.value,
      ldapDn: this.ldapDn?.value,
      ldapTimeout: this.ldapTimeout?.value,
      ldapStatus: this.ldapStatus?.value,
      approvalResultMail: this.approvalResultMail?.value,
      pageTitle: this.pageTitle?.value
    } as DPB0161Req
    if (this._fileSrc) reqBody.iconFile = this._fileSrc;
    this.serverService.updateIdPInfo_ldap(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} AC LDAP IdP User`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.queryIdPInfoList_ldap();
        this.changePage('query');
      }
    })
  }

  async delete(rowData: DPB0159Item) {
    const code = ['cfm_del',];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `${rowData.id}`,
      accept: () => {

        this.serverService.deleteIdPInfo_ldap({ id: rowData.id }).subscribe(async res => {
          const code = ['message.delete', 'message.success', 'message.user'];
          const dict = await this.toolService.getDict(code);
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.delete']} AC LDAP IdP User`,
              detail: `${dict['message.delete']} ${dict['message.success']}!`
            });
            this.queryIdPInfoList_ldap();
            this.changePage('query');

          }
        })
      }
    });
  }

  public get ldapUrl() { return this.form.get('ldapUrl'); };
  public get ldapBaseDn() { return this.form.get('ldapBaseDn'); };
  public get ldapDn() { return this.form.get('ldapDn'); };
  public get ldapTimeout() { return this.form.get('ldapTimeout'); };
  public get ldapStatus() { return this.form.get('ldapStatus'); };
  public get iconFile() { return this.form.get('iconFile'); };
  public get approvalResultMail() { return this.form.get('approvalResultMail'); };
  public get pageTitle() { return this.form.get('pageTitle'); };
}

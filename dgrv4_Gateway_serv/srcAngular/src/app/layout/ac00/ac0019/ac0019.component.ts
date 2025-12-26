import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DPB0179IdPInfoItem } from 'src/app/models/api/ServerService/dpb0179.interface';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import * as dayjs from 'dayjs';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0181LdapDataItem, DPB0181Req } from 'src/app/models/api/ServerService/dpb0181.interface';
import { DPB0180Req, DPB0180Resp } from 'src/app/models/api/ServerService/dpb0180.interface';
import { DPB0182LdapDataItem, DPB0182Req } from 'src/app/models/api/ServerService/dpb0182.interface';
import { DomSanitizer } from '@angular/platform-browser';

@Component({
  selector: 'app-ac0019',
  templateUrl: './ac0019.component.html',
  styleUrls: ['./ac0019.component.css'],
  providers: [ConfirmationService]
})
export class Ac0019Component extends BaseComponent implements OnInit {

  currentTitle: string = this.title;
  currentAction: string = '';
  pageNum: number = 1;
  cols: { field: string; header: string; }[] = [];
  dataList: Array<DPB0179IdPInfoItem> = [];

  form!: FormGroup;
  _fileSrc: any = null;

  idpInfoDetail?: DPB0180Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private toolService: ToolService,
    private serverService: ServerService,
    private messageService: MessageService,
    private fb: FormBuilder,
    private alertService: AlertService,
    private confirmationService: ConfirmationService,
    private sanitizer: DomSanitizer
  ) {
    super(route, tr);
  }

  ngOnInit(): void {

    this.form = this.fb.group({
      status: new FormControl(),
      ldapTimeout: new FormControl(),
      policy: new FormControl(),
      approvalResultMail: new FormControl(),
      iconFile: new FormControl(),
      pageTitle: new FormControl(),
      ldapDataList: new FormControl(),
    });

    this.queryIdPInfoList_mldap();

    //因為該欄位value清空時值變成null但不會觸發欄位檢核，因此強制改為空白字串
    this.ldapTimeout.valueChanges.subscribe(res=>{
      if(res==null) this.ldapTimeout.setValue('')
    })
  }

  queryIdPInfoList_mldap() {
    this.serverService.queryIdPInfoList_mldap().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.dataList = res.RespBody.idPInfoList;
      } else this.dataList = [];
    })
  }

  formateDate(date: Date) {
    return dayjs(date).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(date).format('YYYY-MM-DD HH:mm:ss') : '';
  }

  headerReturn() {
    this.changePage('query');
  }

  async changePage(action: string, rowData?: DPB0179IdPInfoItem) {
    const code = ['button.delete', 'button.create', 'button.update', 'button.detail'];
    const dict = await this.toolService.getDict(code);

    this.currentAction = action;
    this.resetFormValidator(this.form);
    this.form.enable();
    this.clearFile();

    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        break;
      case 'create':
        this.serverService.createIdPInfo_mldap_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
            this.currentTitle = `${this.title} > ${dict['button.create']}`;
            this.pageNum = 2;
            this.status.markAsTouched();
            this.policy.markAsTouched();
          }
        })

        break;
      case 'detail':
        let reqDetail = {
          masterId: rowData?.id
        } as DPB0180Req;
        this.serverService.queryIdPInfoDetailByPk_mldap(reqDetail).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.idpInfoDetail = res.RespBody;
            this.currentTitle = `${this.title} > ${dict['button.detail']}`;
            this.pageNum = 2;
            this.form.disable();
            this.status.setValue(res.RespBody.status);
            this.ldapTimeout.setValue(res.RespBody.ldapTimeout);
            this.policy.setValue(res.RespBody.policy);
            this.approvalResultMail.setValue(res.RespBody.approvalResultMail);
            this.pageTitle.setValue(res.RespBody.pageTitle);

            if (res.RespBody.iconFile) this._fileSrc = res.RespBody.iconFile;

            this.ldapDataList.setValue(res.RespBody.ldapDataList);


          }
        })
        break;

      case 'update':
        let reqD = {
          masterId: rowData?.id
        } as DPB0180Req;
        this.serverService.queryIdPInfoDetailByPk_mldap(reqD).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.idpInfoDetail = res.RespBody;
            this.status.setValue(res.RespBody.status);
            this.ldapTimeout.setValue(res.RespBody.ldapTimeout);
            this.policy.setValue(res.RespBody.policy);
            this.approvalResultMail.setValue(res.RespBody.approvalResultMail);
            this.pageTitle.setValue(res.RespBody.pageTitle);

            if (res.RespBody.iconFile) this._fileSrc = res.RespBody.iconFile;

            this.ldapDataList.setValue(res.RespBody.ldapDataList);

            this.serverService.updateIdPInfo_mldap_before().subscribe(valid =>{
              if (this.toolService.checkDpSuccess(valid.ResHeader)) {
                this.addFormValidator(this.form, valid.RespBody.constraints);
                this.currentTitle = `${this.title} > ${dict['button.update']}`;
                this.pageNum = 2;
              }
            })






          }
        })
        break;
      case 'delete':
          this.delete(rowData!);
          break;
    }
  }

  openFileBrowser() {
    $('#fileName').click();
  }

  checkLdapDataListValid() {
    return Array.isArray(this.ldapDataList.value) ? (this.ldapDataList.value).every(item => item.valid == true) : false;
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

        this._fileSrc = fileReader.result;

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

  create() {
    let reqBody = {
      status: this.status.value,
      ldapTimeout: this.ldapTimeout.value,
      policy: this.policy.value,
      approvalResultMail: this.approvalResultMail.value,
      pageTitle: this.pageTitle.value,
      ldapDataList: this.ldapDataList.value.map(item => {
        return {
          orderNo: item.orderNo,
          ldapUrl: item.ldapUrl,
          ldapBaseDn: item.ldapBaseDn,
          ldapDn: item.ldapDn
        } as DPB0181LdapDataItem
      })
    } as DPB0181Req;

    if (this._fileSrc) reqBody.iconFile = this._fileSrc;
    this.serverService.createIdPInfo_mldap(reqBody).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success', 'message.user'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} AC MLDAP IdP User`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.queryIdPInfoList_mldap();
        this.changePage('query')
      }
    })
  }

  update() {
    let reqBody = {
      masterId: this.idpInfoDetail?.masterId,
      status: this.status.value,
      ldapTimeout: this.ldapTimeout.value,
      policy: this.policy.value,
      approvalResultMail: this.approvalResultMail.value,
      pageTitle: this.pageTitle.value,
      ldapDataList: this.ldapDataList.value.map(item => {
        return {
          orderNo: item.orderNo,
          ldapUrl: item.ldapUrl,
          ldapBaseDn: item.ldapBaseDn,
          ldapDn: item.ldapDn,
          detailId: item.detailId
        } as DPB0182LdapDataItem
      })
    } as DPB0182Req;
    if (this._fileSrc) reqBody.iconFile = this._fileSrc;
    this.serverService.updateIdPInfo_mldap(reqBody).subscribe( async res=>{
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} AC LDAP IdP User`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.queryIdPInfoList_mldap();
        this.changePage('query');
      }
    })
  }

  async delete(rowData: DPB0179IdPInfoItem) {
    const code = ['cfm_del',];
    const dict = await this.toolService.getDict(code);
    this.confirmationService.confirm({
      header: dict['cfm_del'],
      message: `${rowData.id}`,
      accept: () => {

        this.serverService.deleteIdPInfo_mldap({ masterId: rowData.id }).subscribe(async res => {
          const code = ['message.delete', 'message.success', 'message.user'];
          const dict = await this.toolService.getDict(code);
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success', summary: `${dict['message.delete']} AC LDAP IdP User`,
              detail: `${dict['message.delete']} ${dict['message.success']}!`
            });
            this.queryIdPInfoList_mldap();
            this.changePage('query');

          }
        })
      }
    });
  }

  public get status() { return this.form.get('status')!; };
  public get ldapTimeout() { return this.form.get('ldapTimeout')!; };
  public get policy() { return this.form.get('policy')!; };
  public get approvalResultMail() { return this.form.get('approvalResultMail')!; };
  public get pageTitle() { return this.form.get('pageTitle')!; };
  public get ldapDataList() { return this.form.get('ldapDataList')!; };

}

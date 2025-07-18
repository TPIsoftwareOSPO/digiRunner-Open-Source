import { Component, OnInit } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ConfirmationService, MessageService } from 'primeng/api';
import { ServerService } from 'src/app/shared/services/api-server.service';
import { DPB0190Resp, DPB0190RespItem } from 'src/app/models/api/ServerService/dpb0190.interface';
import * as dayjs from 'dayjs';
import { AlertService } from 'src/app/shared/services/alert.service';
import { DPB0192Req } from 'src/app/models/api/ServerService/dpb0192.interface';
import { DPB0200Req } from 'src/app/models/api/ServerService/dpb0200.interface';
import { AlertType } from 'src/app/models/common.enum';

export enum RDBinit {
  connectName = 'APIM-default-DB'
}

@Component({
  selector: 'app-rdb-connection',
  templateUrl: './rdb-connection.component.html',
  styleUrls: ['./rdb-connection.component.css'],
  providers: [MessageService, ConfirmationService]
})
export class RdbConnectionComponent extends BaseComponent implements OnInit {

  currentTitle = this.title;
  pageNum: number = 1;

  formEdit!: FormGroup;
  currentAction: string = '';
  tableData: Array<DPB0190RespItem> = [];

  idbcTip: string = `MySQL:
jdbc:mysql://{{hostname}}:{{port}}/{{databaseName}}
PostgreSQL:
jdbc:postgresql://{{hostname}}:{{port}}/{{databaseName}}
Oracle:
jdbc:oracle:thin:@{{hostname}}:{{port}}:{{databaseName}}
SQL Server:
jdbc:sqlserver://{{hostname}}:{{port}};databaseName={{databaseName}}; trustServerCertificate=true
MariaDB:
jdbc:mariadb://{{hostname}}/{{db}}`

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private confirmationService: ConfirmationService,
    private serverService: ServerService,
    private alertService: AlertService,
    private messageService: MessageService,
  ) {
    super(route, tr);
  }

  ngOnInit(): void {

    this.formEdit = this.fb.group({
      connectionName: new FormControl(''),
      jdbcUrl: new FormControl(''),
      userName: new FormControl(''),
      mima: new FormControl(''),
      maxPoolSize: new FormControl(''),
      connectionTimeout: new FormControl(''),
      idleTimeout: new FormControl(''),
      maxLifetime: new FormControl(''),
      dataSourceProperty: new FormControl([]),
      createDateTime: new FormControl(''),
      createUser: new FormControl(''),
      updateDateTime: new FormControl(''),
      updateUser: new FormControl(''),
    });

    this.serverService.queryRdbConnectionInfoList_ignore1298().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.infoList;
      }
    })
  }

  axios_queryRdbConnectionInfoList() {
    this.serverService.queryRdbConnectionInfoList().subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = res.RespBody.infoList;
      }
      else{
        this.tableData = [];
      }
    })
  }

  checkIsRDBInitConnectName (name:string){
    return (name == RDBinit.connectName);
  }

  headerReturn() {
    this.changePage('query');
  }

  formateDate(date: Date) {
    if (!date) return '';
    const procDate = Number(date);
    return dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') != 'Invalid Date' ? dayjs(procDate).format('YYYY-MM-DD HH:mm:ss') : '';
  }

  async changePage(action: string, rowData?: DPB0190RespItem) {
    const codes = ['button.detail', 'button.create', 'button.update', 'cfm_del', 'message.delete', 'message.success'];
    const dict = await this.toolService.getDict(codes);
    this.resetFormValidator(this.formEdit);
    this.currentAction = action;
    this.formEdit.enable();

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        break;
      case 'create':
        this.serverService.createRdbConnectionInfo_before().subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.create']}`
            this.pageNum = 2;
            this.addFormValidator(this.formEdit, res.RespBody.constraints);
            this.maxPoolSize.setValue(10);
            this.connectionTimeout.setValue(30000);
            this.idleTimeout.setValue(600000);
            this.maxLifetime.setValue(1800000);
          }
        })
        break;
      case 'detail':
        this.serverService.queryRdbConnectionInfoDetail({ connectionName: rowData!.connectionName }).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.currentTitle += `> ${dict['button.detail']}`
            this.pageNum = 2;
            this.formEdit.disable();
            this.connectionName.setValue(res.RespBody.connectionName);
            this.jdbcUrl.setValue(res.RespBody.jdbcUrl);
            this.userName.setValue(res.RespBody.userName);
            this.mima.setValue(res.RespBody.mima);
            this.maxPoolSize.setValue(res.RespBody.maxPoolSize);
            this.connectionTimeout.setValue(res.RespBody.connectionTimeout);
            this.idleTimeout.setValue(res.RespBody.idleTimeout);
            this.maxLifetime.setValue(res.RespBody.maxLifetime);
            this.dataSourceProperty.setValue(res.RespBody.dataSourceProperty);

            this.createDateTime.setValue(this.formateDate(new Date(res.RespBody.createDateTime)));
            this.createUser.setValue(res.RespBody.createUser);
            this.updateDateTime.setValue( res.RespBody.updateDateTime ? this.formateDate(new Date(res.RespBody.updateDateTime)):'');
            this.updateUser.setValue(res.RespBody.updateUser);
          }
        })
        break;
      case 'update':
        this.serverService.queryRdbConnectionInfoDetail({ connectionName: rowData!.connectionName }).subscribe(res => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.serverService.updateRdbConnectionInfo_before().subscribe(resValid => {
              this.currentTitle += `> ${dict['button.update']}`
              this.pageNum = 2;
              this.addFormValidator(this.formEdit, resValid.RespBody.constraints);
              this.connectionName.disable();
              this.connectionName.setValue(res.RespBody.connectionName);
              this.jdbcUrl.setValue(res.RespBody.jdbcUrl);
              this.userName.setValue(res.RespBody.userName);
              this.mima.setValue(res.RespBody.mima);
              this.maxPoolSize.setValue(res.RespBody.maxPoolSize);
              this.connectionTimeout.setValue(res.RespBody.connectionTimeout);
              this.idleTimeout.setValue(res.RespBody.idleTimeout);
              this.maxLifetime.setValue(res.RespBody.maxLifetime);
              this.dataSourceProperty.setValue(res.RespBody.dataSourceProperty);

              if(this.connectionName.value == RDBinit.connectName){
                this.formEdit.disable();
              }
            })
          }
        })
        break;
      case 'delete':
        this.confirmationService.confirm({
          header: dict['cfm_del'],
          message: `${rowData?.connectionName}`,
          accept: () => {

            this.serverService.deleteRdbConnectionInfo({ connectionName: rowData!.connectionName }).subscribe(async res => {

              if (this.toolService.checkDpSuccess(res.ResHeader)) {
                this.messageService.add({
                  severity: 'success', summary: `${dict['message.delete']} RDB connection`,
                  detail: `${dict['message.delete']} ${dict['message.success']}!`
                });

                this.axios_queryRdbConnectionInfoList();
                this.changePage('query');
              }
            })

          }
        });
        break;
    }
  }

  async create() {
    const code = ['property_keyvalue_required'];
    const dict = await this.toolService.getDict(code);

    // 檢查request header 欄位有無空白

    if (this.dataSourceProperty.value) {
      // console.log('header', this.reqHeader.value)
      const checkReqHeaderData = JSON.parse(this.dataSourceProperty.value)
      const checkReqHeader = checkReqHeaderData.every(headerData => {
        return (Object.keys(headerData).map(key => {
          return key != '' && headerData[key] != ''
        }))[0];
      });

      if (!checkReqHeader) {
        this.alertService.ok(dict['property_keyvalue_required'], '');
        return;
      }
    }

    let reqC = {
      connectionName: this.connectionName.value,
      jdbcUrl: this.jdbcUrl.value,
      userName: this.userName.value,
      maxPoolSize: this.maxPoolSize.value,
      connectionTimeout: this.connectionTimeout.value,
      idleTimeout: this.idleTimeout.value,
      maxLifetime: this.maxLifetime.value,
      dataSourceProperty: this.dataSourceProperty.value
    } as DPB0192Req;
    if (this.mima.value) reqC.mima = this.mima.value;

    this.serverService.createRdbConnectionInfo(reqC).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.create']} RDB connection`,
          detail: `${dict['message.create']} ${dict['message.success']}!`
        });

        this.axios_queryRdbConnectionInfoList();
        this.changePage('query');
      }
    })


  }

  async update() {
    const code = ['property_keyvalue_required'];
    const dict = await this.toolService.getDict(code);

    // 檢查request header 欄位有無空白

    if (this.dataSourceProperty.value) {
      // console.log('header', this.reqHeader.value)
      const checkReqHeaderData = JSON.parse(this.dataSourceProperty.value)
      const checkReqHeader = checkReqHeaderData.every(headerData => {
        return (Object.keys(headerData).map(key => {
          return key != '' && headerData[key] != ''
        }))[0];
      });

      if (!checkReqHeader) {
        this.alertService.ok(dict['property_keyvalue_required'], '');
        return;
      }
    }

    let reqU = {
      connectionName: this.connectionName.value,
      jdbcUrl: this.jdbcUrl.value,
      userName: this.userName.value,
      maxPoolSize: this.maxPoolSize.value,
      connectionTimeout: this.connectionTimeout.value,
      idleTimeout: this.idleTimeout.value,
      maxLifetime: this.maxLifetime.value,
      dataSourceProperty: this.dataSourceProperty.value
    } as DPB0192Req;
    if (this.mima.value) reqU.mima = this.mima.value;

    this.serverService.updateRdbConnectionInfo(reqU).subscribe(async res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success'];
        const dict = await this.toolService.getDict(code);
        this.messageService.add({
          severity: 'success', summary: `${dict['message.update']} RDB connection`,
          detail: `${dict['message.update']} ${dict['message.success']}!`
        });

        this.axios_queryRdbConnectionInfoList();
        this.changePage('query');
      }
    })
  }

  testConnect() {
    let req = {
      jdbcUrl: this.jdbcUrl.value,
      userName: this.userName.value,
      mima: this.mima.value,
      connName: this.connectionName.value,
    } as DPB0200Req
    this.serverService.testRdbConnection(req).subscribe( res=> {
      if(this.toolService.checkDpSuccess(res.ResHeader)){
        // console.log(res.RespBody)
        if(res.RespBody.success){

          this.alertService.ok(res.RespBody.msg,'',AlertType.success)
        }
        else{
          this.alertService.ok(res.RespBody.msg,'',AlertType.error)
        }
      }

    })
  }

  public get connectionName() { return this.formEdit.get('connectionName')!; };
  public get jdbcUrl() { return this.formEdit.get('jdbcUrl')!; };
  public get userName() { return this.formEdit.get('userName')!; };
  public get mima() { return this.formEdit.get('mima')!; };
  public get maxPoolSize() { return this.formEdit.get('maxPoolSize')!; };
  public get connectionTimeout() { return this.formEdit.get('connectionTimeout')!; };
  public get idleTimeout() { return this.formEdit.get('idleTimeout')!; };
  public get maxLifetime() { return this.formEdit.get('maxLifetime')!; };
  public get dataSourceProperty() { return this.formEdit.get('dataSourceProperty')!; };
  public get createDateTime() { return this.formEdit.get('createDateTime')!; };
  public get createUser() { return this.formEdit.get('createUser')!; };
  public get updateDateTime() { return this.formEdit.get('updateDateTime')!; };
  public get updateUser() { return this.formEdit.get('updateUser')!; };

}

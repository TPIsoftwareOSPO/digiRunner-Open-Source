import { DialogService } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { DPB0111Req, DPB0111Item } from 'src/app/models/api/RoleService/dpb0111.interface';
import { RoleService } from 'src/app/shared/services/api-role.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0112Req } from 'src/app/models/api/RoleService/dpb0112.interface';
import { DPB0115Req } from 'src/app/models/api/RoleService/dpb0115.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { RoleListLovComponent } from 'src/app/shared/role-list-lov/role-list-lov.component';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { DPB0110Req } from 'src/app/models/api/RoleService/dpb0110.interface';
import { DPB0113Req } from 'src/app/models/api/RoleService/dpb0113.interface';
import { TranslateService } from '@ngx-translate/core';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { DPB0114Req } from 'src/app/models/api/RoleService/dpb0114.interface';

@Component({
  selector: 'app-ac1202',
  templateUrl: './ac1202.component.html',
  styleUrls: ['./ac1202.component.css'],
  providers: [ConfirmationService]
})
export class Ac1202Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立、3：更新
  form: FormGroup;
  listTypeOptionForQuery: { label: string; value: string; }[] = [];
  listTypeOptionNotQuery: { label: string; value: string; }[] = [];
  rtMappingCols: { field: string; header: string }[] = [];
  rtMappingList?: Array<DPB0111Item> = [];
  rtMappingListRowcount: number = 0;
  delData?: DPB0111Item;
  canCreate: boolean = false;
  canUpdate: boolean = false;
  canDelete: boolean = false;
  dialogTitle: string = '';

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private tool: ToolService,
    private list: ListService,
    private roleService: RoleService,
    private message: MessageService,
    private translate: TranslateService,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr);
    this.form = this.fb.group({
      keyword: new FormControl(''),
      listType: new FormControl('all'),
      roleId: new FormControl(''),
      roleAlias: new FormControl({ value: '', disabled: true }),
      txId: new FormControl(''),
      newListType: new FormControl(''),
      newRoleId: new FormControl(''),
      newRoleAlias: new FormControl({ value: '', disabled: true }),
      newTxId: new FormControl(''),
      oriRoleId: new FormControl(''),
      oriListType: new FormControl(''),
      oriTxIdList: new FormControl('')
    });
  }

  ngOnInit() {

    let ReqBody = {
      encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('RT_MAP_LIST_TYPE')) + ',' + 23,
      isDefault: 'N'
    } as DPB0047Req;
    this.list.querySubItemsByItemNo(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['all'];
        const dict = await this.tool.getDict(code);
        let _listTypeOptionForQuery = [
          { label: dict['all'], value: 'all' }
        ];
        res.RespBody.subItems?.map(item => {
          _listTypeOptionForQuery.push({ label: item.subitemName, value: item.subitemNo });
          this.listTypeOptionNotQuery.push({ label: item.subitemName, value: item.subitemNo });
        });
        this.listTypeOptionForQuery = _listTypeOptionForQuery;
      }
    });
    this.roleService.queryRTMapByUk({ txIdList: ['DPB0110', 'DPB0113', 'DPB0114'] } as DPB0115Req).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.canCreate = res.RespBody.dataList.find(item => item.txId === 'DPB0110') ? res.RespBody.dataList.find(item => item.txId === 'DPB0110')!.available : false;
        this.canUpdate = res.RespBody.dataList.find(item => item.txId === 'DPB0113') ? res.RespBody.dataList.find(item => item.txId === 'DPB0113')!.available : false;
        this.canDelete = res.RespBody.dataList.find(item => item.txId === 'DPB0114') ? res.RespBody.dataList.find(item => item.txId === 'DPB0114')!.available : false;
      }
    });
    this.init();
  }

  async changePage(action: string, rowData?: DPB0111Item) {
    const code = ['button.create', 'button.update'];
    const dict = await this.tool.getDict(code);
    this.resetFormValidator(this.form);
    switch (action) {
      case 'query':
        this.currentTitle = this.title;
        this.pageNum = 1;
        this.listType!.setValue('all');
        break;
      case 'create':
        this.currentTitle = `${this.title} > ${dict['button.create']}`;
        this.pageNum = 2;
        this.roleService.createRTMap_before().subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.form, res.RespBody.constraints);
          }
        });
        break;
      case 'update':
        let ReqBody = {
          roleId: rowData!.roleId,
          listType: this.tool.Base64Encoder(this.tool.BcryptEncoder(rowData!.listType)) + ',' + this.convertListTypeIndex(rowData!.listType)
        } as DPB0112Req;
        this.roleService.queryRTMapByPk(ReqBody).subscribe(res => {
          if (this.tool.checkDpSuccess(res.ResHeader)) {
            this.oriRoleId!.setValue(res.RespBody.oriRoleId);
            this.oriListType!.setValue(res.RespBody.oriListType);
            this.oriTxIdList!.setValue(res.RespBody.oriTxIdList);
            this.newRoleId!.setValue(res.RespBody.oriRoleId);
            this.newRoleAlias!.setValue(`${res.RespBody.oriRoleName} - ${res.RespBody.oriRoleAlias}`)
            this.newTxId!.setValue(res.RespBody.oriTxIdString);
            this.newListType!.setValue(res.RespBody.oriListType);
            this.roleService.updateRTMap_before().subscribe(res => {
              if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.addFormValidator(this.form, res.RespBody.constraints);
                this.currentTitle = `${this.title} > ${dict['button.update']}`;
                this.pageNum = 3;
              }
            });
          }
        });
        break;
    }
  }

  async init() {
    const code = ['role_name', 'role_desc', 'txid', 'list_type'];
    const dict = await this.tool.getDict(code);
    this.rtMappingCols = [
      { field: 'roleName', header: dict['role_name'] },
      { field: 'roleAlias', header: dict['role_desc'] },
      { field: 'txId', header: dict['txid'] },
      { field: 'listTypeName', header: dict['list_type'] }
    ]
    this.rtMappingList = [];
    this.rtMappingListRowcount = this.rtMappingList.length;
    let ReqBody = {
      keyword: this.keyword!.value,
      listType: this.listType!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.listType!.value)) + ',' + this.convertListTypeIndex(this.listType!.value) : ''
    } as DPB0111Req;
    this.roleService.queryRTMapList_ignore1298(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rtMappingList = res.RespBody.dataList;
        this.rtMappingListRowcount = this.rtMappingList.length;
      }
    });
  }

  queryRTMappingList() {
    this.rtMappingList = [];
    this.rtMappingListRowcount = this.rtMappingList.length;
    let ReqBody = {
      keyword: this.keyword!.value,
      listType: this.listType!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.listType!.value)) + ',' + this.convertListTypeIndex(this.listType!.value) : ''
    } as DPB0111Req;
    this.roleService.queryRTMapList(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rtMappingList = res.RespBody.dataList;
        this.rtMappingListRowcount = this.rtMappingList.length;
      }
    });
  }

  moreRTMappingList() {
    let ReqBody = {
      p_roleId: this.rtMappingList![this.rtMappingList!.length - 1].roleId,
      p_listType: this.rtMappingList![this.rtMappingList!.length - 1].listType,
      keyword: this.keyword!.value,
      listType: this.listType!.value != 'all' ? this.tool.Base64Encoder(this.tool.BcryptEncoder(this.listType!.value)) + ',' + this.convertListTypeIndex(this.listType!.value) : ''
    } as DPB0111Req;
    this.roleService.queryRTMapList(ReqBody).subscribe(res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        this.rtMappingList = this.rtMappingList!.concat(res.RespBody.dataList);
        this.rtMappingListRowcount = this.rtMappingList.length;
      }
    });
  }

  createRTMapping() {
    let ReqBody = {
      roleId: this.roleId!.value,
      txId: this.txId!.value,
      listType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.listType!.value)) + ',' + this.convertListTypeIndex(this.listType!.value)
    } as DPB0110Req;
    this.roleService.createRTMap(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['message.create', 'message.success', 'message.role_role_mapping', 'txid'];
        const dict = await this.tool.getDict(code);
        this.message.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.role_role_mapping']}${dict['txid']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
        this.listType!.setValue('all');
        this.queryRTMappingList();
        this.changePage('query');
      }
    });
  }

  updateRTMapping() {
    let ReqBody = {
      oriRoleId: this.oriRoleId!.value,
      oriTxIdList: this.oriTxIdList!.value,
      oriListType: this.oriListType!.value,
      newRoleId: this.newRoleId!.value,
      newTxId: this.newTxId!.value,
      newListType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.newListType!.value)) + ',' + this.convertListTypeIndex(this.newListType!.value)
    } as DPB0113Req;
    this.roleService.updateRTMap(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const code = ['message.update', 'message.success', 'message.role_role_mapping', 'txid'];
        const dict = await this.tool.getDict(code);
        this.message.add({ severity: 'success', summary: `${dict['message.update']} ${dict['message.role_role_mapping']}${dict['txid']}`, detail: `${dict['message.update']} ${dict['message.success']}!` });
        this.listType!.setValue('all');
        this.queryRTMappingList();
        this.changePage('query');
      }
    });
  }

  showDialog(rowData: DPB0111Item) {
    const codes = ['dialog.delete', 'message.success', 'message.delete', 'cfm_del_role_txid_mapping'];
    this.translate.get(codes).pipe(
      switchMap(dict => this.openDialog$(rowData, dict))
    ).subscribe();
  }

  openDialog$(rowData: DPB0111Item, dict: any): Observable<boolean> {
    return Observable.create(obser => {
      this.delData = rowData;
      // this.message.clear();
      // this.message.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_role_txid_mapping'] });
      this.confirmationService.confirm({
        header: ' ',
        message: dict['cfm_del_role_txid_mapping'],
        accept: () => {
            this.onDeleteConfirm();
        }
      });
      // obser.next(true);
    });
  }

  onDeleteConfirm() {
    this.message.clear();
    let ReqBody = {
      roleId: this.delData!.roleId,
      listType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.delData!.listType)) + ',' + this.convertListTypeIndex(this.delData!.listType)
    } as DPB0114Req;
    this.roleService.deleteRTMap(ReqBody).subscribe(async res => {
      if (this.tool.checkDpSuccess(res.ResHeader)) {
        const codes = ['message.delete', '', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        this.message.clear();
        this.message.add({ severity: 'success', summary: `${dicts['message.delete']} ${dicts['']}`, detail: `${dicts['message.delete']} ${dicts['message.success']}!` });
        this.queryRTMappingList();
      }
    });
  }

  onReject() {
    this.message.clear();
  }

  convertListTypeIndex(type: string): number {
    switch (type) {
      case 'W':
        return 0;
      case 'B':
        return 1;
      default:
        return -1
    }
  }

  async copyOriData(identifData: string) {
    const code = ['copy', 'data', 'message.success'];
    const dict = await this.tool.getDict(code);
    let selBox = document.createElement('textarea');
    selBox.style.position = 'fixed';
    selBox.style.left = '0';
    selBox.style.top = '0';
    selBox.style.opacity = '0';
    selBox.value = identifData;
    document.body.appendChild(selBox);
    selBox.focus();
    selBox.select();
    document.execCommand('copy');
    document.body.removeChild(selBox);
    this.message.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
  }

  async queryRoleList() {
    const code = ['role_lists'];
    const dict = await this.tool.getDict(code);
    this.dialogTitle = dict['role_lists'];
    // let data: FormParams = {
    //     displayInDialog: true,
    //     data: { selectionMode: 'single' },
    //     afterCloseCallback: (res) => {
    //         if (res) {
    //             // for create
    //             this.roleId!.setValue(res.roleID);
    //             this.roleAlias!.setValue(`${res.roleName} - ${res.roleAlias}`);
    //             // for update
    //             this.newRoleId!.setValue(res.roleID);
    //             this.newRoleAlias!.setValue(`${res.roleName} - ${res.roleAlias}`);
    //         }
    //         else {
    //             this.roleId!.setValue(null);
    //             this.roleAlias!.setValue(null);
    //             this.newRoleId!.setValue(null);
    //             this.newRoleAlias!.setValue(null);
    //         }
    //     }
    // }
    // this._dialog.open(RoleListLovComponent, data);

    const ref = this.dialogService.open(RoleListLovComponent, {
      data: { selectionMode: 'single' },
      header: dict['role_list'],
      width: '700px'
    })

    ref.onClose.subscribe(res => {
      if (res) {
        console.log(res)
        // for create
        this.roleId!.setValue(res.roleId);
        this.roleAlias!.setValue(`${res.roleName} - ${res.roleAlias}`);
        // for update
        this.newRoleId!.setValue(res.roleId);
        this.newRoleAlias!.setValue(`${res.roleName} - ${res.roleAlias}`);
      }
      else {
        this.roleId!.setValue(null);
        this.roleAlias!.setValue(null);
        this.newRoleId!.setValue(null);
        this.newRoleAlias!.setValue(null);
      }

    });
  }

  headerReturn(){
    this.changePage('query');
  }

  originStringTable(item: DPB0111Item) {
    return (item.oriTxId!=item.txId && item.isTxIdTruncated) ? item.txId: item.oriTxId;
  }

  switchOri(item: DPB0111Item) {
    item.isTxIdTruncated = !item.isTxIdTruncated;
  }

  public get keyword() { return this.form.get('keyword'); }
  public get listType() { return this.form.get('listType'); }
  public get roleId() { return this.form.get('roleId'); }
  public get roleAlias() { return this.form.get('roleAlias'); }
  public get txId() { return this.form.get('txId'); }
  public get newListType() { return this.form.get('newListType'); }
  public get newRoleId() { return this.form.get('newRoleId'); }
  public get newRoleAlias() { return this.form.get('newRoleAlias'); }
  public get newTxId() { return this.form.get('newTxId'); }
  public get oriRoleId() { return this.form.get('oriRoleId'); }
  public get oriListType() { return this.form.get('oriListType'); }
  public get oriTxIdList() { return this.form.get('oriTxIdList'); }

}

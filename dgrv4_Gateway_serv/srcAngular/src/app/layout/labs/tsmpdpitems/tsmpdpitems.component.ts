import {
  DPB9908Item,
  DPB9908Req,
} from './../../../models/api/ServerService/dpb9908.interface';
import {
  DPB9907ItemValid,
  DPB9907Req,
} from './../../../models/api/ServerService/dpb9907.interface';
import {
  DPB9906Item,
  DPB9906ItemValid,
  DPB9906Req,
} from './../../../models/api/ServerService/dpb9906.interface';
import {
  DPB9909Req,
  DPB9909Item,
} from './../../../models/api/ServerService/dpb9909.interface';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import { ListService } from 'src/app/shared/services/api-list.service';
import { ServerService } from './../../../shared/services/api-server.service';
import { ApiBaseService } from './../../../shared/services/api-base.service';
import { AlertService } from 'src/app/shared/services/alert.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ApiService } from 'src/app/shared/services/api-api.service';
import { FileService } from 'src/app/shared/services/api-file.service';
import { LocaleComponent } from './locale/locale.component';
import { ParamItemComponent } from './param-item/param-item.component';
import { LocaleItemComponent } from './locale-item/locale-item.component';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { Observable } from 'rxjs';
import { MessageService, SelectItem, ConfirmationService } from 'primeng/api';
import { TranslateService } from '@ngx-translate/core';
// import { ModuleService } from 'srcAngular/app/shared/services/api-module.service';
import { ActivatedRoute, Router } from '@angular/router';
import * as dayjs from 'dayjs';
import { generate } from 'generate-password';
import {
  Component,
  OnInit,
  ViewChild,
  ViewContainerRef,
  ComponentFactoryResolver,
} from '@angular/core';
import { BaseComponent } from '../../base-component';
import {
  DPB9905Item,
  DPB9905Req,
  DPB9905Subitem,
} from 'src/app/models/api/ServerService/dpb9905.interface';
import { DPB9906Resp } from 'src/app/models/api/ServerService/dpb9906.interface';
import {
  DPB9907Resp,
  DPB9907Item,
} from 'src/app/models/api/ServerService/dpb9907.interface';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { SubitemsComponent } from './subitems/subitems.component';
import * as ValidatorFns from 'src/app/shared/validator-functions';
import * as FileSaver from 'file-saver';
import { AlertType, TxID } from 'src/app/models/common.enum';
import { NgxUiLoaderService } from 'ngx-ui-loader';

interface Subitems extends DPB9906Item {
  valid: boolean;
}

interface localeItem extends DPB9907Item {
  valid: boolean;
}

@Component({
  selector: 'app-tsmpdpitems',
  templateUrl: './tsmpdpitems.component.html',
  styleUrls: ['./tsmpdpitems.component.css'],
  providers: [FileService, ApiService, ConfirmationService],
})
export class TsmpdpitemsComponent extends BaseComponent implements OnInit {
  onChange: (value: any) => void = (_: any) => {};
  @ViewChild('localeitem', { read: ViewContainerRef })
  localeitemRef!: ViewContainerRef;
  @ViewChild('paramitem', { read: ViewContainerRef })
  paramitemRef!: ViewContainerRef;
  @ViewChild('locale', { read: ViewContainerRef }) localeRef!: ViewContainerRef;
  @ViewChild('subitems', { read: ViewContainerRef })
  subitemsRef!: ViewContainerRef;

  currentTitle: string = this.title;
  pageNum: number = 1; // 1：查詢、2：建立
  form!: FormGroup;
  subitemForm!: FormGroup;
  detailForm!: FormGroup;
  itemForm!: FormGroup;
  toastValue: any;
  cols: { field: string; header: string }[] = [];
  tableData: Array<DPB9905Item> = [];
  tableDataRowcount: number = 0;
  delData?: DPB9905Item;
  selectedItem: DPB9905Item = {
    itemOrder: null,
    itemNo: '',
    itemName: '',
    updateDateTime: '',
    updateUser: '',
    subitemCount: ',',
  };
  currentAction: string = '';
  selectLocale: string = '';

  localeList: { label: string; value: string }[] = [];

  subCols: { field: string; header: string }[] = [];
  subTableData: Array<DPB9905Subitem> = [];
  subTableRowcount: number = 0;
  selectedSubItem: DPB9905Subitem = {
    subitemOrder: '',
    sortBy: null,
    subitemNo: '',
    subitemName: '',
    updateDateTime: '',
    updateUser: '',
    isDefault: null,
  };

  subitemDetailCols: { field: string; header: string }[] = [];

  subitemDetail: DPB9906Resp = {
    locale: '',
    itemId: null,
    sortBy: null,
    isDefault: '',
    itemNo: '',
    itemName: '',
    subitemNo: '',
    subitemName: '',
    params: [],
    subitemNameList: [],
    paramSize: 0,
  };
  subitemsValid: boolean = true;
  localeValid: boolean = true;

  updateItem?: DPB9907Resp;

  fileName: string = '';
  file: any = null;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private fb: FormBuilder,
    private toolService: ToolService,
    private translateService: TranslateService,
    private messageService: MessageService,
    private alertService: AlertService,
    // private moduleService: ModuleService,
    private fileService: FileService,
    private api: ApiBaseService,
    private apiService: ApiService,
    private serverService: ServerService,
    private listService: ListService,
    private router: Router,
    private confirmationService: ConfirmationService,
    private ngxService: NgxUiLoaderService
  ) {
    super(route, tr);
  }

  async ngOnInit() {
    // this.checkOrgId();

    this.form = this.fb.group({
      keyword: new FormControl(''),
      locale: new FormControl(''),
    });

    this.subitemForm = this.fb.group({
      keyword: new FormControl(''),
      locale: new FormControl(''),
    });

    this.detailForm = this.fb.group({
      locale: new FormControl(''),
      subitemNo: new FormControl(''),
      itemNo: new FormControl(''),
      subitemNameList: new FormControl([]),
      isDefault: new FormControl(null),
    });

    this.itemForm = this.fb.group({
      itemNo: new FormControl('', ValidatorFns.requiredValidator()),
    });

    let ReqBody = {
      encodeItemNo:
        this.toolService.Base64Encoder(
          this.toolService.BcryptEncoder('RTN_CODE_LOCALE')
        ) +
        ',' +
        22,
      isDefault: 'N',
    } as DPB0047Req;
    this.listService.querySubItemsByItemNo(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (res.RespBody.subItems) {
          this.localeList = res.RespBody.subItems.map((item) => {
            return { label: item.subitemName, value: item.subitemNo };
          });
        }
      }
    });

    // this.locale.valueChanges.subscribe(res => {
    //   console.log(res)
    //   this.queryTsmpDpItemsList();
    // })

    this.locale!.setValue(navigator.language);

    const code = [
      'cate_order',
      'cate_no',
      'cate_name',
      'last_update_datetime',
      'last_update_user',
      'subitem_count',
      'subitem_order',
      'default_cr',
      'subitem_seqno',
      'subitem_name',
      'param_no',
      'param_value',
      'subitem_available',
      'subitem_used',
      'subitem_total',
    ];
    const dict = await this.toolService.getDict(code);
    this.cols = [
      { field: 'itemOrder', header: dict['cate_order'] },
      { field: 'itemNo', header: dict['cate_no'] },
      { field: 'itemName', header: dict['cate_name'] },
      { field: 'updateDateTime', header: dict['last_update_datetime'] },
      { field: 'updateUser', header: dict['last_update_user'] },
      {
        field: 'subitemCount',
        header:
          dict['subitem_available'] +
          '/' +
          dict['subitem_used'] +
          '/' +
          dict['subitem_total'],
      },
    ];
    this.tableData = [];
    this.tableDataRowcount = this.tableData.length;

    this.subCols = [
      { field: 'subitemOrder', header: dict['subitem_order'] },
      { field: 'subitemNo', header: dict['subitem_seqno'] },
      { field: 'subitemName', header: dict['subitem_name'] },
      { field: 'updateDateTime', header: dict['last_update_datetime'] },
      { field: 'updateUser', header: dict['last_update_user'] },
      { field: 'isDefault', header: dict['default_cr'] },
    ];
    this.subTableData = [];
    this.subTableRowcount = this.subTableData.length;

    this.queryTsmpDpItemsList();
  }

  checkOrgId() {
    const tokenPool = this.toolService.getToken().split('.');
    const token = this.toolService.Base64Decoder(tokenPool[1]);
    const tokenParse = JSON.parse(token);

    if (tokenParse.org_id !== '100000') {
      this.router.navigateByUrl('/about');
    }
  }

  queryTsmpDpItemsList() {
    let ReqBody = {
      locale: this.locale.value,
      keyword: this.keyword.value,
    } as DPB9905Req;

    if (this.pageNum > 1) {
      (ReqBody.locale = this.sub_locale.value),
        (ReqBody.keyword = this.sub_keyword.value),
        (ReqBody.itemNo = this.selectedItem.itemNo);
    }

    this.serverService.queryTsmpDpItemsList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        if (this.pageNum > 1) {
          this.selectedItem = res.RespBody.itemList[0];
          this.subTableData = res.RespBody.itemList[0].subitemList!;
          this.subTableRowcount = this.subTableData.length;
        } else {
          this.tableData = res.RespBody.itemList;
          this.tableDataRowcount = this.tableData.length;
        }
      } else {
        if (this.pageNum > 1) {
          this.subTableData = [];
          this.subTableRowcount = this.subTableData.length;
        } else {
          this.tableData = [];
          this.tableDataRowcount = this.tableData.length;
        }
      }
    });
  }

  localeChange() {
    this.queryTsmpDpItemsList();
  }

  async changePage(
    action: string,
    rowData?: DPB9905Item,
    subRowData?: DPB9905Subitem
  ) {
    this.currentAction = action;
    const code = [
      'subitem_list',
      'button.update',
      'button.delete',
      'subitem_detail',
      'subitem_detail_update',
    ];
    const dict = await this.toolService.getDict(code);
    this.selectLocale = this.locale.value;
    // this.resetFormValidator(this.form);
    // this.resetFormValidator(this.detailForm);

    switch (action) {
      case 'query':
        this.pageNum = 1;
        this.currentTitle = this.title;
        this.locale.setValue(this.selectLocale);
        this.queryTsmpDpItemsList();
        break;
      case 'subitem':
        this.currentTitle = `${this.title} > ${dict['subitem_list']}`;
        this.pageNum = 2;
        this.sub_locale.setValue(this.selectLocale);
        this.sub_keyword.setValue('');
        if (rowData) this.selectedItem = rowData;
        this.queryTsmpDpItemsList();
        break;
      case 'subitemDetail':
        this.currentTitle = `${this.title} > ${dict['subitem_list']} > ${dict['subitem_detail']}`;
        this.locale.setValue(this.selectLocale);
        this.queryTsmpDpItemsDetail(subRowData!);
        this.pageNum = 3;
        break;
      case 'subItemUpdate':
        this.currentTitle = `${this.title} > ${dict['subitem_list']} > ${dict['subitem_detail_update']}`;

        this.locale.setValue(this.selectLocale);
        this.serverService.updateTsmpDpItemsDetail_before().subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.addFormValidator(this.detailForm, res.RespBody.constraints);
            // console.log(this.detailForm)
          }
        });
        this.queryTsmpDpItemsDetail(subRowData!, true);
        this.pageNum = 4;
        break;
      case 'itemUpdate':
        this.currentTitle = `${this.title} > ${dict['button.update']}`;
        this.pageNum = 5;
        this.locale.setValue(this.selectLocale);
        this.queryItemNameList(rowData!);
        break;
      case 'export':
        this.ngxService.start();
        this.serverService.exportTsmpDpItems().subscribe((res) => {
          if (res.type === 'application/json') {
            const reader = new FileReader();
            reader.onload = () => {
              const jsonData = JSON.parse(reader.result as string);
              this.alertService.ok(
                jsonData.ResHeader.rtnMsg,
                '',
                AlertType.warning,
                jsonData.ResHeader.txDate + '<br>' + jsonData.ResHeader.txID
              );
            };
            reader.readAsText(res);
          } else {
            const data: Blob = new Blob([res], {
              type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=UTF-8',
            });

            const date = dayjs(new Date()).format('YYYYMMDD_HHmm');
            FileSaver.saveAs(data, `Items_${date}.xlsx`);
          }
          this.ngxService.stop();
        });
    }
  }

  getMoreData() {
    let ReqBody = {
      p_itemOrder: this.tableData[this.tableData.length - 1].itemOrder,
      locale: this.locale.value,
      keyword: this.keyword.value,
    } as DPB9905Req;
    this.serverService.queryTsmpDpItemsList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.tableData = this.tableData.concat(res.RespBody.itemList);
        this.tableDataRowcount = this.tableData.length;
      }
    });
  }

  getMoreSubData() {
    let ReqBody = {
      itemNo: this.selectedItem.itemNo,
      locale: this.locale.value,
      keyword: this.keyword.value,
      p_sortBy: this.subTableData[this.subTableData.length - 1].sortBy,
    } as DPB9905Req;
    this.serverService.queryTsmpDpItemsList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.subTableData = this.subTableData.concat(
          res.RespBody.itemList[0].subitemList!
        );
        this.subTableRowcount = this.subTableData.length;
      }
    });
  }

  onToastClose(event) {
    this.toastValue = '';
  }

  async procData() {
    // console.log(this.currentAction)
    const code = [
      'message.create',
      'subitem_detail_update',
      'message.success',
      'message.update',
    ];
    const dict = await this.toolService.getDict(code);
    switch (this.currentAction) {
      case 'subItemUpdate':
        // console.log(this.isDefault.value);

        let reqBodyU = {
          itemNo: this.itemNo.value,
          isDefault: this.isDefault.value ? 'V' : null,
          oriSubitemNo: this.subitemDetail.oriSubitemNo,
          subitemNo: this.subitemNo.value,
          subitemNameList: this.subitemDetail.subitemNameList.map(
            (res: DPB9906ItemValid) => {
              return {
                version: res.version,
                locale: res.locale,
                subitemName: res.subitemName,
                params: res.params,
              } as DPB9909Item;
            }
          ),
          // params: this.subitemDetail.params
        } as DPB9909Req;

        // console.log(reqBodyU)
        // return;
        this.serverService
          .updateTsmpDpItemsDetail(reqBodyU)
          .subscribe((res) => {
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({
                severity: 'success',
                summary: `${dict['message.update']} ${dict['subitem_detail_update']}`,
                detail: `${dict['message.update']} ${dict['message.success']}!`,
              });
              this.changePage('subitem');
            }
          });

        break;
      case 'itemUpdate':
        // console.log(this.updateItem)
        let itemReqBodyU = {
          itemNo: this.itemForm.get('itemNo')!.value,
          oriItemNo: this.updateItem!.oriItemNo,
          dataList: this.updateItem!.dataList.map((item: DPB9907ItemValid) => {
            return {
              locale: item.locale,
              itemName: item.itemName,
            } as DPB9908Item;
          }),
        } as DPB9908Req;

        this.serverService.updateItemNameList(itemReqBodyU).subscribe((res) => {
          if (this.toolService.checkDpSuccess(res.ResHeader)) {
            this.messageService.add({
              severity: 'success',
              summary: `${dict['message.update']}`,
              detail: `${dict['message.update']} ${dict['message.success']}!`,
            });
            this.changePage('query');
          }
        });

        break;
    }
  }

  async deleteUser() {
    const code = ['cfm_del_setting', 'system_alert'];
    const dict = await this.toolService.getDict(code);
    // this.messageService.add({ key: 'deleteMsg', sticky: true, severity: 'error', summary: dict['cfm_del_setting'] });
    this.confirmationService.confirm({
      header: dict['system_alert'],
      message: dict['cfm_del_setting'],
      accept: () => {
        this.deleteConfirm();
      },
    });
  }

  deleteConfirm() {
    // this.messageService.clear();
    // let ReqBody = {
    //   id: this.id.value,
    // } as DGL9904Req;
    // this.fileService.deleteTsmpSetting(ReqBody).subscribe(async res => {
    //   if (this.toolService.checkDpSuccess(res.ResHeader)) {
    //     const code = ['message.delete', 'key', 'message.success'];
    //     const dict = await this.toolService.getDict(code);
    //     this.messageService.add({
    //       severity: 'success', summary: `${dict['message.delete']} ${dict['key']}`,
    //       detail: `${dict['message.delete']} ${dict['message.success']}!`
    //     });
    //     // this.queryTsmpSettingList();
    //     this.changePage('query');
    //   }
    // });
  }

  queryTsmpDpItemsDetail(rowData: DPB9905Subitem, update?: boolean) {
    let reqBody = {
      locale: this.locale.value,
      itemNo: this.selectedItem.itemNo,
      subitemNo: rowData.subitemNo,
    } as DPB9906Req;
    if (update) {
      reqBody.getSubitemNameList = 'Y';
    }
    this.serverService.queryTsmpDpItemsDetail(reqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.subitemDetail = res.RespBody;
        this.subitemDetail.oriSubitemNo = res.RespBody.subitemNo;
        this.itemNo.setValue(res.RespBody.itemNo);
        this.subitemNo.setValue(res.RespBody.subitemNo);
        this.isDefault.setValue(res.RespBody.isDefault == 'V');
        this.locale.setValue(res.RespBody.locale);
        this.subitemNameList.setValue(res.RespBody.subitemNameList);

        if (this.currentAction == 'subItemUpdate') {
          // this.localeitemRef.clear();

          this.subitemsRef.clear();
          this.subitemDetail.subitemNameList.forEach((item, index) => {
            // this.generateLocaleItem(item, index)
            this.generateSubitems(item, index, res.RespBody.paramSize);
          });

          // this.paramitemRef.clear();
          // this.subitemDetail.params?.forEach((item, index) => {
          //   this.generateParamItem(item, index)
          // });
        }
      }
    });
  }

  subitemDetaillocaleChange() {
    let reqBody = {
      locale: this.subitemDetail.locale,
      itemNo: this.subitemDetail.itemNo,
      subitemNo: this.subitemDetail.subitemNo,
    } as DPB9906Req;
    this.serverService.queryTsmpDpItemsDetail(reqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.subitemDetail = res.RespBody;
        // this.locale.setValue(this.subitemDetail.locale);
      }
    });
  }

  // generateLocaleItem(DPB9909Item: DPB9909Item, idx: number) {

  // var componentFactory = this.factoryResolver.resolveComponentFactory(LocaleItemComponent);
  // let componentRef = this.localeitemRef.createComponent(LocaleItemComponent);

  // componentRef.instance.itemValue = DPB9909Item;

  // componentRef.instance.change.subscribe((res: DPB9909Item) => {
  //   this.subitemDetail.subitemNameList[idx].version = res.version;
  //   this.subitemDetail.subitemNameList[idx].locale = res.locale;
  //   this.subitemDetail.subitemNameList[idx].subitemName = res.subitemName;
  // });

  // }

  generateSubitems(DPB9909Item: DPB9909Item, idx: number, paramSize: number) {
    let _subitemsRef = this.subitemsRef.createComponent(SubitemsComponent);
    _subitemsRef.instance.itemIdx = idx;
    _subitemsRef.instance.itemValue = DPB9909Item;
    _subitemsRef.instance.paramSize = paramSize;
    _subitemsRef.instance.change.subscribe((res: Subitems) => {
      this.subitemDetail.subitemNameList[idx].version = res.version;
      this.subitemDetail.subitemNameList[idx].locale = res.locale;
      this.subitemDetail.subitemNameList[idx].subitemName = res.subitemName;
      this.subitemDetail.subitemNameList[idx].params = res.params;
      this.subitemDetail.subitemNameList[idx].valid = res.valid;
      this.subitemsValid = this.subitemDetail.subitemNameList
        .filter((x) => {
          return typeof x.valid !== 'undefined';
        })
        .every((item) => item.valid);
    });
  }

  // generateParamItem(param: string, idx: number) {
  //   // var componentFactory = this.factoryResolver.resolveComponentFactory(ParamItemComponent);
  //   let componentRef = this.paramitemRef.createComponent(ParamItemComponent);

  //   componentRef.instance.itemValue = { index: idx, value: param };

  //   componentRef.instance.change.subscribe((res: { index: number, value: string }) => {
  //     this.subitemDetail.params[idx] = res.value;
  //   });
  // }

  queryItemNameList(rowData: DPB9905Item) {
    let ReqBody = {
      itemNo: rowData.itemNo,
    } as DPB9907Req;
    this.localeRef.clear();
    this.serverService.queryItemNameList(ReqBody).subscribe((res) => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.updateItem = res.RespBody;
        this.updateItem.oriItemNo = res.RespBody.itemNo;
        this.itemForm.get('itemNo')!.setValue(res.RespBody.itemNo);

        res.RespBody.dataList.forEach((item, index) => {
          this.generateLocale(item, index);
        });
      }
    });
  }

  generateLocale(DPB9907Item: DPB9907Item, idx: number) {
    // var componentFactory = this.factoryResolver.resolveComponentFactory(LocaleComponent);
    let componentRef = this.localeRef.createComponent(LocaleComponent);

    componentRef.instance.itemValue = DPB9907Item;

    componentRef.instance.change.subscribe((res: localeItem) => {
      this.updateItem!.dataList[idx].itemName = res.itemName;
      this.updateItem!.dataList[idx].valid = res.valid;

      this.localeValid = this.updateItem!.dataList.filter((x) => {
        return typeof x.valid !== 'undefined';
      }).every((item) => item.valid);
    });
  }

  onReject() {
    this.messageService.clear();
  }

  headerReturn() {
    console.log(this.pageNum);

    if (this.pageNum == 3 || this.pageNum == 4) {
      this.changePage('subitem');
    } else {
      this.changePage('query');
    }
  }

  async fileChange(event: any) {
    let file: FileList = event.target.files;
    const code = [
      'uploading',
      'cfm_img_format',
      'cfm_size',
      'message.success',
      'upload_result',
      'waiting',
    ];
    const dict = await this.toolService.getDict(code);
    if (file.length != 0) {
      // let fileReader = new FileReader();
      // fileReader.onloadend = () => {
      // this.message.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
      // this.fileData!.setValue(file.item(0));
      // console.log(this.fileData.value)

      // }
      // fileReader.readAsBinaryString(file.item(0)!);
      this.file = file.item(0);
      this.fileName = file[0].name;
      event.target.value = '';
    } else {
      // this.fileData!.setValue(null);
      this.file = null;
      event.target.value = '';
    }
  }

  openFileBrowser() {
    $('#file').click();
  }

  importTsmpDpItems() {
    const req = {
      ReqHeader: this.api.getReqHeader(TxID.importTsmpDpItems),
      ReqBody: {},
    };

    this.serverService
      .importTsmpDpItems(req, this.file)
      .subscribe(async (res) => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          const code = ['uploading', 'message.success', 'upload_result'];
          const dict = await this.toolService.getDict(code);
          this.messageService.add({
            severity: 'success',
            summary: dict['upload_result'],
            detail: `${dict['message.success']}!`,
          });
          this.file = null;
          this.fileName = '';
          this.queryTsmpDpItemsList();
        }
      });
  }

  public get keyword() {
    return this.form.get('keyword')!;
  }
  public get locale() {
    return this.form.get('locale')!;
  }
  public get sub_keyword() {
    return this.subitemForm.get('keyword')!;
  }
  public get sub_locale() {
    return this.subitemForm.get('locale')!;
  }
  public get subitemNo() {
    return this.detailForm.get('subitemNo')!;
  }
  public get itemNo() {
    return this.detailForm.get('itemNo')!;
  }
  public get subitemNameList() {
    return this.detailForm.get('subitemNameList')!;
  }
  public get isDefault() {
    return this.detailForm.get('isDefault')!;
  }
  public get itemForm_itemNo() {
    return this.itemForm.get('itemNo')!;
  }
}

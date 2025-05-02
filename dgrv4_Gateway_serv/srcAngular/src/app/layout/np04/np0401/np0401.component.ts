import { DialogService } from 'primeng/dynamicdialog';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { Component, OnInit, ViewChild, ViewContainerRef, ComponentFactoryResolver } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormGroup, FormBuilder, FormControl } from '@angular/forms';
import { FormOperate } from 'src/app/models/common.enum';
import { DPB0067RespItem, DPB0067Req } from 'src/app/models/api/RequisitionService/dpb0067.interface';
import { DPB0068Resp, DPB0068Req, DPB0068D2 } from 'src/app/models/api/RequisitionService/dpb0068.interface';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { RequisitionService } from 'src/app/shared/services/api-requisition.service';
import { ToolService } from 'src/app/shared/services/tool.service';
import { TranslateService } from '@ngx-translate/core';
import { ApiSignOffService } from 'src/app/shared/services/api-api-sign-off.service';
import { ListService } from 'src/app/shared/services/api-list.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DPB0047Req } from 'src/app/models/api/ListService/dpb0047.interface';
import * as dayjs from 'dayjs';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { DPB0069Req, DPB0069Resp } from 'src/app/models/api/RequisitionService/dpb0069.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { ApiOnOffComponent } from '../../np03/np0301/api-on-off/api-on-off.component';
import { ClientAuthorizeApiComponent } from '../../np03/np0303/client-authorize-api/client-authorize-api.component';
import { RequisitionFormComponent } from './requisition-form/requisition-form.component';
import { DPB0071Req } from 'src/app/models/api/ApiSignOffService/dpb0071.interface';
import { DPB0066Req, DPB0066ApiOnOff, DPB0066ApiBindingData } from 'src/app/models/api/RequisitionService/dpb0066.interface';
import { OpenApiKeyFormComponent } from '../../np03/np0304/open-api-key-form/open-api-key-form.component';

@Component({
    selector: 'app-np0401',
    templateUrl: './np0401.component.html',
    styleUrls: ['./np0401.component.css'],
    providers: [ConfirmationService]
})
export class Np0401Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;
    @ViewChild('requisitionViewRef', { read: ViewContainerRef }) requisitionViewRef!: ViewContainerRef;

    form!: FormGroup;
    formOperate = FormOperate;
    dataList: Array<DPB0067RespItem> = [];
    cols: { field: string; header: string }[] = [];
    rowcount: number = 0;
    dialogTitle: string = '';
    display: boolean = false;
    reviewBtnFlag: boolean = false;
    closeBtnFlag: boolean = false;
    resendBtnFlag: boolean = false;
    comment: string = '';
    requisitionDetail?: DPB0068Resp;
    reqTypes: { label: string; value: string; }[] = [];
    subTypes: { label: string; value: string; }[] = [];
    tabTitles?: { label: string; value: string; }[];
    currentQuyType: string = '';

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private requisition: RequisitionService,
        private tool: ToolService,
        private translate: TranslateService,
        // private factoryResolver: ComponentFactoryResolver,
        private apiSignOff: ApiSignOffService,
        private listService: ListService,
        private message: MessageService,
        private ngxService: NgxUiLoaderService,
        private dialogService:DialogService,
        private confirmationService:ConfirmationService
    ) {
        super(route, tr);
    }

    async ngOnInit() {
        this.form = this.fb.group({
            keyword: new FormControl(''),
            startDate: new FormControl(''),
            endDate: new FormControl(''),
            encodeReqType: new FormControl('all'),
            encodeReqSubtype: new FormControl('all')
        });
        this.converDateInit();
        const code = ['apply_date', 'all', 'apply_type', 'apply_user', 'message.layer', 'status', 'case_number'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'createDateTime', header: dict['apply_date'] },
            { field: 'reqOrderNo', header: dict['case_number'] },
            { field: 'applyUserName', header: dict['apply_user'] },
            { field: 'applyType', header: dict['apply_type'] },
            { field: 'chkStatus', header: dict['status'] }
        ];
        // 申請類別 - 大項目
        // let ReqBody = {
        //     encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('REVIEW_TYPE')) + ',' + 4,
        //     isDefault: 'N'
        // } as DPB0047Req;
        // this.listService.querySubItemsByItemNo(ReqBody).subscribe(res => {
        //     if (this.tool.checkDpSuccess(res.ResHeader)) {
        //         let _reqTypes:{label:string, value:string}[] = [
        //             { label: dict['all'], value: 'all' }
        //         ];
        //         res.RespBody.subItems?.map(item => {
        //             _reqTypes.push({ label: item.subitemName, value: item.subitemNo });
        //         });
        //         this.reqTypes = _reqTypes;
        //     }
        // });
        // this.encodeReqType!.valueChanges.subscribe(type => {
        //     this.subTypes = [];
        //     this.encodeReqSubtype!.setValue('all');
        //     if (type == 'API_ON_OFF' || type == 'OPEN_API_KEY') {
        //         // 申請類別 - 小項目
        //         let ReqBody = {
        //             isDefault: 'N'
        //         } as DPB0047Req;
        //         switch (type) {
        //             case 'API_ON_OFF':
        //                 ReqBody.encodeItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder('API_ON_OFF')) + ',' + 5;
        //                 break;
        //             case 'OPEN_API_KEY':
        //                 ReqBody.encodeItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder('OPEN_API_KEY')) + ',' + 21;
        //                 break;
        //         }
        //         this.listService.querySubItemsByItemNo(ReqBody).subscribe(res => {
        //             if (this.tool.checkDpSuccess(res.ResHeader)) {
        //                 let _subTypes = [
        //                     { label: dict['all'], value: 'all' }
        //                 ];
        //                 res.RespBody.subItems?.map(item => {
        //                     _subTypes.push({ label: item.subitemName, value: item.subitemNo });
        //                 });
        //                 this.subTypes = _subTypes;
        //             }
        //         });
        //     }
        // });
        // Tab種類
        let reqbody = {
            encodeItemNo: this.tool.Base64Encoder(this.tool.BcryptEncoder('ORDERM_QUY_TYPE')) + ',' + 13,
            isDefault: 'N'
        } as DPB0047Req;
        this.listService.querySubItemsByItemNo(reqbody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                let _tabTitles:{label:string, value:string}[] = [];
                res.RespBody.subItems?.map(item => {
                    _tabTitles.push({ label: item.subitemName, value: item.subitemNo });
                });
                this.tabTitles = _tabTitles;
                this.currentQuyType = this.tabTitles[0].value;
                this.dataList = [];
                this.rowcount = this.dataList.length;
                let ReqBody = {
                    keyword: this.keyword!.value,
                    startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
                    endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
                    encodeQuyType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.currentQuyType)) + ',' + this.convertQuyTypeIndex(this.currentQuyType)
                } as DPB0067Req;
                if (this.encodeReqType!.value != 'all') {
                    ReqBody.encodeReqType = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqType!.value)) + ',' + this.convertReqTypeIndex(this.encodeReqType!.value);
                }
                if (this.encodeReqSubtype!.value != 'all') {
                    ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqSubtype!.value)) + ',' + this.convertReqSubtypeIndex(this.encodeReqSubtype!.value);
                }
                this.requisition.queryReqLikeList_ignore1298(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.dataList = res.RespBody.dataList;
                        this.rowcount = this.dataList.length;
                    }
                });
            }
        });
        this.startDate?.valueChanges.subscribe(res => {
          if(!res){
             this.startDate?.setValue(new Date(),{ emitEvent: false })
             return;
          }
          const sDate = new Date(res);
          sDate.setHours(0,0,0,0);
          const eDate = new Date(this.endDate?.value);
          eDate.setHours(0,0,0,0);
          if(eDate<sDate) this.endDate?.setValue(sDate)
        })
        this.endDate?.valueChanges.subscribe(res => {
          if(!res){
            this.endDate?.setValue(new Date(),{ emitEvent: false })
            return;
         }
          const eDate = new Date(res);
          eDate.setHours(0,0,0,0);
          const sDate = new Date(this.startDate?.value);
          sDate.setHours(0,0,0,0);
          if(eDate<sDate) this.startDate?.setValue(eDate)
        })
    }

    converDateInit() {
        let date = new Date();
        this.startDate!.setValue(this.tool.addDay(date, -6));
        this.endDate!.setValue(date);
    }

    submitForm() {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        let ReqBody = {
            keyword: this.keyword!.value,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            encodeQuyType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.currentQuyType)) + ',' + this.convertQuyTypeIndex(this.currentQuyType)
        } as DPB0067Req;
        if (this.encodeReqType!.value != 'all') {
            ReqBody.encodeReqType = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqType!.value)) + ',' + this.convertReqTypeIndex(this.encodeReqType!.value);
        }
        if (this.encodeReqSubtype!.value != 'all') {
            ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqSubtype!.value)) + ',' + this.convertReqSubtypeIndex(this.encodeReqSubtype!.value);
        }
        this.ngxService.start();
        this.requisition.queryReqLikeList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = res.RespBody.dataList;
                this.rowcount = this.dataList.length;
            }
            this.ngxService.stopAll();
        });
    }

    showDialog(rowData: DPB0067RespItem, operation: FormOperate) {
        const codes = ['dialog.update', 'dialog.traker', 'message.update', 'message.requisition.label', 'message.success', 'message.close_app', 'message.resend', 'button.review', 'cfm_send', 'message.requisition.send_ps','button.edit'];
        this.translate.get(codes).pipe(
            switchMap(dict => this.openDialog$(rowData, operation, dict))
        ).subscribe();
    }

    openDialog$(rowData: DPB0067RespItem, operation: FormOperate, dict: any): Observable<boolean> {
        let ReqBody = {
            reqOrdermId: rowData.reqOrdermId,
            encodeQuyType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.currentQuyType)) + ',' + this.convertQuyTypeIndex(this.currentQuyType)
        } as DPB0068Req;
        return new Observable(obser => {
            switch (operation) {
                case FormOperate.review: // reviewVisiable -> ACCEPT、RETURN、DENIED
                    this.comment = '';
                    this.requisition.queryReqByPk(ReqBody).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            this.reviewBtnFlag = true;
                            this.closeBtnFlag = false;
                            this.resendBtnFlag = false;
                            this.requisitionDetail = res.RespBody;
                            this.dialogTitle = dict['button.review'];
                            this.createRequisitionRef(operation, this.requisitionDetail);
                            obser.next(true);
                        }
                    });
                    break;
                case FormOperate.traker: // trakerVisiable
                    this.requisition.queryReqByPk(ReqBody).subscribe(detailDataRes => {
                        if (this.tool.checkDpSuccess(detailDataRes.ResHeader)) {
                            this.dialogTitle = dict['dialog.traker'];
                            let trakerReqBody = {
                                reqOrdermId: rowData.reqOrdermId
                            } as DPB0069Req;
                            this.requisition.queryHistoryByPk(trakerReqBody).subscribe(trakerDataRes => {
                                if (this.tool.checkDpSuccess(trakerDataRes.ResHeader)) {
                                    this.reviewBtnFlag = false;
                                    this.closeBtnFlag = false;
                                    this.resendBtnFlag = false;
                                    this.requisitionDetail = detailDataRes.RespBody;
                                    this.createRequisitionRef(operation, this.requisitionDetail, trakerDataRes.RespBody);
                                    obser.next(true);
                                }
                            });
                        }
                    });
                    break;
                case FormOperate.settle: // closeVisiable -> END
                    this.requisition.queryReqByPk(ReqBody).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            this.reviewBtnFlag = false;
                            this.closeBtnFlag = true;
                            this.resendBtnFlag = false;
                            this.requisitionDetail = res.RespBody;
                            this.dialogTitle = dict['message.close_app'];
                            this.createRequisitionRef(operation, this.requisitionDetail);
                            obser.next(true);
                        }
                    });
                    break;
                case FormOperate.resend: // resendVisiable -> resend
                case FormOperate.update:
                    this.dialogTitle = operation == FormOperate.resend ? dict['message.resend'] : dict['button.edit'];
                    this.requisition.queryReqByPk(ReqBody).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            let data: FormParams = {
                                operate: operation == FormOperate.resend ? FormOperate.resend : FormOperate.update,
                                data: res.RespBody,
                                afterCloseCallback: (r) => {
                                  if(r){
                                    this.message.add({ severity: 'success', summary: `${operation == FormOperate.resend ? dict['message.resend'] : dict['message.update']} ${dict['message.requisition.label']}`, detail: `${operation == FormOperate.resend ? dict['message.resend'] : dict['message.update']} ${dict['message.success']}!` });
                                    this.submitForm();
                                  }
                                }
                            }

                            switch (res.RespBody.reqType) {
                                case 'API_ON_OFF':
                                    this._dialog.open(ApiOnOffComponent, data);
                                    break;
                                case 'CLIENT_REG':
                                    //已移除
                                    break;
                                case 'API_APPLICATION':
                                    // this._dialog.open(ClientAuthorizeApiComponent, data);

                                    const ref = this.dialogService.open(ClientAuthorizeApiComponent,{
                                      autoZIndex: true,
                                      header:dict['button.chs_api'],
                                      data:{
                                        operate: operation == FormOperate.resend ? FormOperate.resend : FormOperate.update,
                                        data: res.RespBody,
                                      }
                                    })

                                    ref.onClose.subscribe(res => {
                                      if(res){
                                      this.message.add({ severity: 'success', summary: `${operation == FormOperate.resend ? dict['message.resend'] : dict['message.update']} ${dict['message.requisition.label']}`, detail: `${operation == FormOperate.resend ? dict['message.resend'] : dict['message.update']} ${dict['message.success']}!` });
                                      this.submitForm();
                                      }
                                    })

                                    break;
                                case 'OPEN_API_KEY':
                                    this._dialog.open(OpenApiKeyFormComponent, data);
                                    break;
                            }
                            obser.next(true);
                        }
                    });
                    break;
                case FormOperate.create:
                    this.requisition.queryReqByPk(ReqBody).subscribe(res => {
                        if (this.tool.checkDpSuccess(res.ResHeader)) {
                            this.requisitionDetail = res.RespBody;
                            // this.message.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['cfm_send'], detail: dict['message.requisition.send_ps'] });
                            this.confirmationService.confirm({
                              header: dict['cfm_send'],
                              message:  dict['message.requisition.send_ps'],
                              accept: () => {
                                  this.onCreateRequisition();
                              }
                            });

                            obser.next(true);
                        }
                    });
                    break;
            }
        })
    }

    createRequisitionRef(operation: number, detailData: DPB0068Resp, trakerData?: DPB0069Resp) {
        this.requisitionViewRef.clear();
        // let componentFactory = this.factoryResolver.resolveComponentFactory(RequisitionFormComponent);
        let componentRef = this.requisitionViewRef.createComponent(RequisitionFormComponent);
        let data: FormParams = {
            operate: operation,
            data: { detailData: detailData, trakerData: trakerData },
            displayInDialog: true
        }
        componentRef.instance.data = data;
        this.display = true;
        componentRef.instance.change.subscribe(res => {
            this.comment = res;
        });
    }

    moreData() {
        let ReqBody = {
            reqOrdermId: this.dataList[this.dataList.length - 1].reqOrdermId,
            keyword: this.keyword!.value,
            startDate: dayjs(this.startDate!.value).format('YYYY/MM/DD'),
            endDate: dayjs(this.endDate!.value).format('YYYY/MM/DD'),
            encodeQuyType: this.tool.Base64Encoder(this.tool.BcryptEncoder(this.currentQuyType)) + ',' + this.convertQuyTypeIndex(this.currentQuyType)
        } as DPB0067Req;
        if (this.encodeReqType!.value != 'all') {
            ReqBody.encodeReqType = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqType!.value)) + ',' + this.convertReqTypeIndex(this.encodeReqType!.value);
        }
        if (this.encodeReqSubtype!.value != 'all') {
            ReqBody.encodeReqSubtype = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.encodeReqSubtype!.value)) + ',' + this.convertReqSubtypeIndex(this.encodeReqSubtype!.value);
        }
        this.requisition.queryReqLikeList(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.dataList = this.dataList.concat(res.RespBody.dataList);
                this.rowcount = this.dataList.length;
            }
        });
    }

    convertReqTypeIndex(reqType: string): number {
        switch (reqType) {
            case 'API_APPLICATION':
                return 0;
            case 'API_ON_OFF':
                return 1;
            case 'CLIENT_REG':
                return 2;
            case 'OPEN_API_KEY':
                return 3;
            default:
              return -1;
        }
    }

    convertReqSubtypeIndex(reqSubType: string): number {
        switch (reqSubType) {
            case 'API_ON':
                return 0;
            case 'API_OFF':
                return 1;
            case 'API_ON_UPDATE':
                return 2;
            case 'OPEN_API_KEY_APPLICA':
                return 0;
            case 'OPEN_API_KEY_UPDATE':
                return 1;
            case 'OPEN_API_KEY_REVOKE':
                return 2;
            default:
                return -1;
        }
    }

    convertQuyTypeIndex(quyType: string): number {
        switch (quyType) {
            case 'REQ':
                return 0;
            case 'EXA':
                return 1;
            case 'REV':
                return 2;
            default:
                return -1;
        }
    }

    changeTab(evn) {
        this.dataList = [];
        this.rowcount = this.dataList.length;
        this.currentQuyType = this.tabTitles![evn.index].value;
        this.submitForm();
    }

    async executeReg(action: string) {
        const codes = ['message.requisition.label', 'message.accept', 'message.return', 'message.denied', 'message.settle', 'message.success'];
        const dicts = await this.tool.getDict(codes);
        let ReqBody = {
            reqOrdermId: this.requisitionDetail?.reqOrdermId,
            chkStatus: this.requisitionDetail?.chkStatus
        } as DPB0071Req;
        switch (action) {
            case 'ACCEPT':
                ReqBody.reqComment = this.comment;
                ReqBody.encodeSubItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(action)) + ',' + 1
                this.apiSignOff.signReq(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.display = false;
                        this.message.add({ severity: 'success', summary: `${dicts['message.requisition.label']} ${dicts['message.accept']}`, detail: `${dicts['message.success']}!` });
                        this.submitForm();
                    }
                });
                break;
            case 'RETURN':
                ReqBody.reqComment = this.comment;
                ReqBody.encodeSubItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(action)) + ',' + 3
                this.apiSignOff.signReq(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.display = false;
                        this.message.add({ severity: 'success', summary: `${dicts['message.requisition.label']} ${dicts['message.return']}`, detail: `${dicts['message.success']}!` });
                        this.submitForm();
                    }
                });
                break;
            case 'DENIED':
                ReqBody.reqComment = this.comment;
                ReqBody.encodeSubItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(action)) + ',' + 2
                this.apiSignOff.signReq(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.display = false;
                        this.message.add({ severity: 'success', summary: `${dicts['message.requisition.label']} ${dicts['message.denied']}`, detail: `${dicts['message.settle']}!` });
                        this.submitForm();
                    }
                });
                break;
            case 'END':
                ReqBody.encodeSubItemNo = this.tool.Base64Encoder(this.tool.BcryptEncoder(action)) + ',' + 5
                this.apiSignOff.signReq(ReqBody).subscribe(res => {
                    if (this.tool.checkDpSuccess(res.ResHeader)) {
                        this.display = false;
                        this.message.add({ severity: 'success', summary: `${dicts['message.requisition.label']} ${dicts['message.settle']}`, detail: `${dicts['message.success']}!` });
                        this.submitForm();
                    }
                });
                break;
        }
    }

    doResend() {
        let ReqBody = {
            reqOrdermId: this.requisitionDetail?.reqOrdermId,
            lv: this.requisitionDetail?.lv,
            act: 'R',
            reqDesc: this.requisitionDetail?.reqDesc,
            effectiveDate: this.requisitionDetail?.effectiveDate,
            apiOnOffD: this.convertApiUidDatas(this.requisitionDetail!.apiOnOff!.apiOnOffList)
        } as DPB0066Req;
        this.requisition.resendReq(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const code = ['message.requisition.label', 'message.resend', 'message.success'];
                const dict = await this.tool.getDict(code);
                this.display = false;
                this.message.add({ severity: 'success', summary: `${dict['message.requisition.label']} ${dict['message.resend']}`, detail: `${dict['message.success']}!` });
                this.submitForm();
            }
        });
    }

    onCreateRequisition() {
        this.message.clear();
        let ReqBody = {
            reqOrdermId: this.requisitionDetail!.reqOrdermId,
            lv: this.requisitionDetail!.lv,
            act: 'S'
        } as DPB0066Req;
        this.requisition.resendReq(ReqBody).subscribe(async res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                const code = ['message.requisition.label', 'message.send', 'message.success'];
                const dict = await this.tool.getDict(code);
                this.display = false;
                this.message.add({ severity: 'success', summary: `${dict['message.send']} ${dict['message.requisition.label']}`, detail: `${dict['message.send']} ${dict['message.success']}!` });
                this.submitForm();
            }
        });
    }

    onReject() {
        this.message.clear();
    }

    convertApiUidDatas(apiList: Array<DPB0068D2>): DPB0066ApiOnOff {
        let _apiUidDatas = {} as DPB0066ApiOnOff;
        let apiUidObj = new Object();
        let oriFileNameObj = new Object();
        let newfileNameObj = new Object();
        for (let api of apiList) {
            let apiBinding = new Array<DPB0066ApiBindingData>();
            for (let themeId in api.themeList) {
                apiBinding.push({ apiUid: api.apiUid, refThemeId: parseInt(themeId) });
            }
            if (Object.keys(api.docFileInfo).length > 0) {
                oriFileNameObj[api.apiUid] = Object.keys(api.docFileInfo)[0]
            }
            apiUidObj[api.apiUid] = apiBinding;
        }
        newfileNameObj = oriFileNameObj;
        _apiUidDatas.apiUidDatas = apiUidObj;
        _apiUidDatas.oriApiMapFileName = oriFileNameObj;
        _apiUidDatas.newApiMapFileName = newfileNameObj;
        _apiUidDatas.encPublicFlag = this.tool.Base64Encoder(this.tool.BcryptEncoder(this.requisitionDetail!.apiOnOff!.publicFlag)) + ',' + this.requisitionDetail!.apiOnOff!.publicFlag
        return _apiUidDatas;
    }

    public get keyword() { return this.form.get('keyword'); };
    public get startDate() { return this.form.get('startDate'); };
    public get endDate() { return this.form.get('endDate'); };
    public get encodeReqType() { return this.form.get('encodeReqType'); };
    public get encodeReqSubtype() { return this.form.get('encodeReqSubtype'); };

}

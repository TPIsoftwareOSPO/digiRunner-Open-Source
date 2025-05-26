import { Component, OnInit } from "@angular/core";
import { BaseComponent } from "../../base-component";
import { ConfirmationService, MessageService } from "primeng/api";
import { ActivatedRoute,Router } from "@angular/router";
import { TransformMenuNamePipe } from "src/app/shared/pipes/transform-menu-name.pipe";
import { ToolService } from "src/app/shared/services/tool.service";
import { ApiService } from "src/app/shared/services/api-api.service";
import { AA0319Req, AA0319ReqItem } from "src/app/models/api/ApiService/aa0319.interface";
import { NgxUiLoaderService } from "ngx-ui-loader";
import { FormBuilder, FormControl, FormGroup } from "@angular/forms";
import { FileService } from "src/app/shared/services/api-file.service";
import { AA0318Item, AA0318Req } from "src/app/models/api/ApiService/aa0318.interface";
import * as base64 from 'js-base64'

@Component({
    selector: 'app-ac0318',
    templateUrl: './ac0318.component.html',
    styleUrls: ['./ac0318.component.css'],
    providers: [MessageService, ApiService, ConfirmationService]
})
export class Ac0318Component extends BaseComponent implements OnInit {

    cols: { field: string; }[] = [];
    selected: Array<AA0318Item> = [];
    rowcount: number= 0;
    form!: FormGroup;
    apiList: Array<AA0318Item> = [];
    fileBatchNo: number = 0;
    apiFile?: File;
    direction: string = "asc";
    currentTitle = this.title;

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private messageService: MessageService,
        private toolService: ToolService,
        private apiService: ApiService,
        private ngxService: NgxUiLoaderService,
        private fb: FormBuilder,
        private fileService: FileService,
        private confirmationService: ConfirmationService,
        private router:Router
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.form = this.fb.group({
            file: new FormControl(),
            fileName: new FormControl({ value: '', disabled: true }),
            fileSize: new FormControl('')
        })
        this.selected = [];
        this.cols = [
            { field: 'apiKey' },
            { field: 'moduleName' },
            { field: 'apiName' },
            { field: 'apiSrc' },
            { field: 'endpoint' },
            { field: 'checkAct' },
            { field: 'result' }
        ];
        let titleArray = this.currentTitle.split("-");
        this.title = titleArray.join(' > ')
    }

    openFileBrowser() {
        $('#file').click();
    }

    changeFile(event) {
        if (event.target.files.length != 0) {
            this.apiFile = event.target.files[0];
            this.fileName.setValue(this.apiFile!.name);
            let _fileSize = this.apiFile!.size / 1024;
            this.fileSize.setValue((Math.round(_fileSize * 100) / 100));
        }
        else {
            this.file.reset();
            this.fileName.setValue('');
            this.fileSize.setValue('');
        }
    }

    uploadFile() {
        let fileReader = new FileReader();
        fileReader.onloadend = () => {
            this.fileService.uploadFile2(this.apiFile!).subscribe(res => {
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                    this.apiList = [];
                    this.rowcount = this.apiList.length;
                    this.ngxService.start();
                    let ReqBody = {
                        tempFileName: res.RespBody.tempFileName
                    } as AA0318Req;
                    this.apiService.uploadRegCompAPIs(ReqBody).subscribe(async resp => {
                        this.ngxService.stop();
                        if (this.toolService.checkDpSuccess(resp.ResHeader)) {
                            const code = ['uploading', 'waiting', 'cfm_size', 'upload_result', 'message.success'];
                            const dict = await this.toolService.getDict(code);
                            this.messageService.add({ severity: 'success', summary: dict['upload_result'], detail: `${dict['message.success']}!` });
                            this.fileBatchNo = resp.RespBody.batchNo;

                            resp.RespBody.apiList.map(rowData=> {
                              if(rowData.srcURLByIpRedirectMap){
                                let tmp = <any>[];
                                Object.keys(rowData.srcURLByIpRedirectMap).sort().map(key=>{
                                  // console.log('key',key);
                                  tmp.push({
                                    ip:key,
                                    srcURL: rowData.srcURLByIpRedirectMap![key]
                                  })
                                })
                                rowData.srcURLByIpRedirectMap = tmp;
                              }
                              return {
                                ...rowData
                              }
                            })
                            this.apiList = resp.RespBody.apiList;
                            // console.log('this.apiList',this.apiList);
                            this.rowcount = this.apiList.length;
                        }
                    });
                }
            });
        }
        fileReader.readAsBinaryString(this.apiFile!);
    }

    selectAll(evn) {
        // this.selected.map((item, index) => {
        //     if (item.checkAct.v == 'N') {
        //         document.getElementsByTagName('p-tableCheckbox')[index].getElementsByClassName('ui-chkbox-box')[0].classList.remove('ui-state-active');
        //         document.getElementsByTagName('p-tableCheckbox')[index].getElementsByClassName('ui-chkbox-icon')[0].classList.remove('pi-check');
        //     }
        // });
        this.selected = this.selected.filter(item => item.checkAct.v != 'N');
    }

    async import() {
        const code = ['button.import', 'data', 'message.success', 'unchecked_api', 'cfm_import', 'message.fail'];
        const dict = await this.toolService.getDict(code);
        if (this.selected.length < this.apiList.length) {
            // this.messageService.add({ key: 'confirm', sticky: true, severity: 'warn', summary: dict['unchecked_api'], detail: dict['cfm_import'] });

            this.confirmationService.confirm({
              header: dict['unchecked_api'],
              message:  dict['cfm_import'],
              accept: () => {
                  this.confirmImport();
              }
            });
        }
        else {
            this.ngxService.start();
            let _apiList = this.selected.map(item => {
                return {
                    moduleName: item.moduleName.t ? item.moduleName.ori : item.moduleName.val,
                    apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val
                } as AA0319ReqItem;
            });
            let ReqBody = {
                batchNo: this.fileBatchNo,
                apiList: _apiList
            } as AA0319Req;
            this.apiService.importRegCompAPIs(ReqBody).subscribe(res => {
                this.ngxService.stop();
                if (this.toolService.checkDpSuccess(res.ResHeader)) {
                    for (let i = 0; i < res.RespBody.apiList.length; i++) {
                        this.selected.map(item => {
                            if (((item.apiKey.t ? item.apiKey.ori : item.apiKey.val) == res.RespBody.apiList[i].apiKey) && ((item.moduleName.t ? item.moduleName.ori : item.moduleName.val) == res.RespBody.apiList[i].moduleName)) {
                                item['result'] = res.RespBody.apiList[i].result;

                                if (res.RespBody.apiList[i].result.v == 'S') {
                                    item['memo'] = {
                                        t: false,
                                        val: ''
                                    };
                                }
                                else {
                                    if (res.RespBody.apiList[i].desc) {
                                        item['memo'] = res.RespBody.apiList[i].desc;
                                    }
                                }
                            }
                        });
                    }
                    if(res.RespBody.apiList.every(item=> item.result.v == 'S')){
                      this.messageService.add({ severity: 'success', summary: `${dict['button.import']} ${dict['message.success']}` });
                    }else{
                      this.messageService.add({ severity: 'error', summary: `${dict['button.import']} ${dict['message.fail']}` });
                    }
                    this.selected = [];
                }
            });
        }
    }

    async confirmImport() {
        this.messageService.clear();
        const code = ['button.import', 'data', 'message.success', 'message.fail'];
        const dict = await this.toolService.getDict(code);
        this.ngxService.start();
        let _apiList = this.selected.map(item => {
            return {
                moduleName: item.moduleName.t ? item.moduleName.ori : item.moduleName.val,
                apiKey: item.apiKey.t ? item.apiKey.ori : item.apiKey.val
            } as AA0319ReqItem;
        });
        let ReqBody = {
            batchNo: this.fileBatchNo,
            apiList: _apiList
        } as AA0319Req;
        this.apiService.importRegCompAPIs(ReqBody).subscribe(res => {
            this.ngxService.stop();
            if (this.toolService.checkDpSuccess(res.ResHeader)) {
                for (let i = 0; i < res.RespBody.apiList.length; i++) {
                    this.selected.map(item => {
                        if (((item.apiKey.t ? item.apiKey.ori : item.apiKey.val) == res.RespBody.apiList[i].apiKey) && ((item.moduleName.t ? item.moduleName.ori : item.moduleName.val) == res.RespBody.apiList[i].moduleName)) {
                            item['result'] = res.RespBody.apiList[i].result;
                            if (res.RespBody.apiList[i].result.v == 'S') {
                                item['memo'] = {
                                    t: false,
                                    val: ''
                                };
                            }
                            else {

                                if (res.RespBody.apiList[i].desc) {
                                    item['memo'] = res.RespBody.apiList[i].desc;
                                }
                            }
                        }
                    });
                }
                if(res.RespBody.apiList.every(item=> item.result.v == 'S')){
                  this.messageService.add({ severity: 'success', summary: `${dict['button.import']} ${dict['message.success']}` });
                }else{
                  this.messageService.add({ severity: 'error', summary: `${dict['button.import']} ${dict['message.fail']}` });
                }
                this.selected = [];
            }
        });
    }

    onReject() {
        this.messageService.clear();
    }

    async copyData(data: string) {
        const code = ['copy', 'data', 'message.success'];
        const dict = await this.toolService.getDict(code);
        let selBox = document.createElement('textarea');
        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = data;
        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();
        document.execCommand('copy');
        document.body.removeChild(selBox);
        this.messageService.add({ severity: 'success', summary: `${dict['copy']} ${dict['data']}`, detail: `${dict['copy']} ${dict['message.success']}` });
    }

    changeSort(field: string) {
        const tempData = this.apiList.slice();
        const isAsc = this.direction === 'asc';
        this.apiList = tempData.sort((a, b) => {
            switch (field) {
                case 'apiKey': return this.compare(a.apiKey.val, b.apiKey.val, isAsc);
                case 'moduleName': return this.compare(a.moduleName.val, b.moduleName.val, isAsc);
                case 'apiName': return this.compare(a.apiName.val, b.apiName.val, isAsc);
                case 'apiSrc': return this.compare(a.apiSrc.v, b.apiSrc.v, isAsc);
                case 'endpoint': return this.compare(a.endpoint, b.endpoint, isAsc);
                default:
                 return -1;
            }
        });
        if (isAsc == true) {
            this.direction = 'desc';
        }
        else {
            this.direction = 'asc';
        }
    }

    decodeSrcUrl(srcUrl){
      if (srcUrl.includes('b64.')) {

        let srcUrlArr = srcUrl.split('.');
        srcUrlArr.shift();


        let srcUrlArrEdit: string[] = [];
        for (let i = 0; i < srcUrlArr.length; i++) {
          if (i % 2 == 0) {
            srcUrlArrEdit.push(srcUrlArr[i]  +'%, '+ base64.Base64.decode(srcUrlArr[i + 1]) );
          }
        }

        return srcUrlArrEdit.join('<br>');

      }
      else{
        return srcUrl;
      }

    }

    headerReturn(){
        this.router.navigate(['/ac03/ac0301/']);
    }

    originStringTable(item: any) {
      return !item.ori ? item.val : item.t ? item.val : item.ori;
    }

    switchOri(item: any) {
      item.t = !item.t;
    }


    public get file() { return this.form.get('file')!; }
    public get fileName() { return this.form.get('fileName')!; }
    public get fileSize() { return this.form.get('fileSize')!; }



}

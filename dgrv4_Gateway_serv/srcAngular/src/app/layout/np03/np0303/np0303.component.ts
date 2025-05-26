import { DialogService } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild } from '@angular/core';
import { BaseComponent } from '../../base-component';
import { ActivatedRoute } from '@angular/router';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { FormBuilder, FormControl, FormGroup } from '@angular/forms';
import { ToolService } from 'src/app/shared/services/tool.service';
import { ClientService } from 'src/app/shared/services/api-client.service';
import { FormOperate } from 'src/app/models/common.enum';
import { AA0203Req } from 'src/app/models/api/ClientService/aa0203.interface';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { MessageService } from 'primeng/api';
import { ClientAuthorizeApiComponent } from './client-authorize-api/client-authorize-api.component';
import { DPB0083RespItem } from 'src/app/models/api/CertificateAuthorityService/dpb0083.interface';
import { DPB0006Req, DPB0006Client } from 'src/app/models/api/MemberService/dpb0006.interface';
import { MemberService } from 'src/app/shared/services/api-member.service';

@Component({
    selector: 'app-np0303',
    templateUrl: './np0303.component.html',
    styleUrls: ['./np0303.component.css']
})
export class Np0303Component extends BaseComponent implements OnInit {

    @ViewChild('dialog') _dialog!: DialogComponent;

    form!: FormGroup;
    cols: { field: string; header: string; }[] = [];
    clientList: Array<DPB0006Client> = new Array<DPB0006Client>();
    rowcount: number = 0;
    dialogTitle: string = '';

    constructor(
        route: ActivatedRoute,
        tr: TransformMenuNamePipe,
        private fb: FormBuilder,
        private tool: ToolService,
        private client: ClientService,
        private messageService: MessageService,
        private memberService: MemberService,
        private dialogService: DialogService,
    ) {
        super(route, tr);
    }

    ngOnInit() {
        this.form = this.fb.group({
            keyword: new FormControl('')
        });
        this.init();
    }

    async init() {
        const code = ['client_id', 'client_name', 'client_alias', 'client_status', 'reg_status', 'api_audience'];
        const dict = await this.tool.getDict(code);
        this.cols = [
            { field: 'clientId', header: dict['client_id'] },
            { field: 'clientName', header: dict['client_name'] },
            { field: 'clientAlias', header: dict['client_alias'] },
            { field: 'statusName', header: dict['client_status'] },
            { field: 'checkPointName', header: dict['reg_status'] },
            { field: 'publicFlagName', header: dict['api_audience'] }
        ];
        this.clientList = [];
        this.rowcount = this.clientList.length;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value
        } as DPB0006Req;
        this.memberService.queryMemberHistory_ignore1298(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientList = res.RespBody.clientList;
                this.rowcount = this.clientList.length;
            }
        });
    }

    submitForm() {
        this.clientList = [];
        this.rowcount = this.clientList.length;
        let ReqBody = {
            keyword: this.form.get('keyword')!.value
        } as DPB0006Req;
        this.memberService.queryMemberHistory(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientList = res.RespBody.clientList;
                this.rowcount = this.clientList.length;
            }
        });
    }

    async showDialog(rowData: DPB0083RespItem) {
        const code = ['button.chs_api', 'message.create', 'message.requisition.client_auth_api', 'message.success'];
        const dict = await this.tool.getDict(code);
        let ReqBody = {
            clientID: rowData.clientId,
            clientName: rowData.clientName
        } as AA0203Req;
        this.dialogTitle = dict['button.chs_api'];
        this.client.queryClientDetail(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                // let data: FormParams = {
                //     operate: FormOperate.create,
                //     data: res.RespBody,
                //     displayInDialog: true,
                //     afterCloseCallback: (r) => {
                //         if (r && this.tool.checkDpSuccess(r.ResHeader)) {
                //             this.submitForm();
                //             this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.requisition.client_auth_api']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
                //         }
                //     }
                // }
                // this._dialog.open(ClientAuthorizeApiComponent, data);

                // const ref = this.dialogService.open(RoleListLovComponent, {
                //   data: { selectionMode: 'single' },
                //   header: dict['role_list'],
                // })

                // ref.onClose.subscribe(res => {
                //   if (res) {
                //     this.roleAlias!.setValue(res.roleAlias);
                //     this.roleName!.setValue(res.roleName);
                //   }
                //   else {
                //     this.roleAlias!.setValue('');l
                //     this.roleName!.setValue('');
                //   }

                // });

                const ref = this.dialogService.open(ClientAuthorizeApiComponent,{
                  autoZIndex: true,
                  header:dict['button.chs_api'],
                  data:{
                    operate: FormOperate.create,
                    data: res.RespBody,
                  }
                })
                ref.onClose.subscribe(res => {
                  if(res && this.tool.checkDpSuccess(res.ResHeader)){
                    this.submitForm();
                    this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.requisition.client_auth_api']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
                  }
                })
            }
        });
    }

    moreData() {
        let ReqBody = {
            clientId: this.clientList[this.clientList.length - 1].clientId,
            keyword: this.form.get('keyword')!.value
        } as DPB0006Req;
        this.memberService.queryMemberHistory(ReqBody).subscribe(res => {
            if (this.tool.checkDpSuccess(res.ResHeader)) {
                this.clientList = this.clientList.concat(res.RespBody.clientList);
                this.rowcount = this.clientList.length;
            }
        });
    }

}

import { DialogService, DynamicDialogRef } from 'primeng/dynamicdialog';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormOperate } from 'src/app/models/common.enum';
import { AA1002List } from 'src/app/models/api/OrgService/aa1002.interface';
import { TranslateService } from '@ngx-translate/core';
import { switchMap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { FormParams } from 'src/app/models/api/form-params.interface';
import { ToolService } from 'src/app/shared/services/tool.service';
import { SidebarService } from '../../components/sidebar/sidebar.service';
import { MessageService, ConfirmationService } from 'primeng/api';
import { DialogComponent } from 'src/app/shared/dialog/dialog.component';
import { OrgFormComponent } from './org-form/org-form.component';
import { TOrgService } from 'src/app/shared/services/org.service';
import { AA1004Req } from 'src/app/models/api/OrgService/aa1004.interface';
import { OrgDetailComponent } from './org-detail/org-detail.component';
import { Router, ActivatedRoute } from '@angular/router';
import { AA1005Req, AA1005Resp } from 'src/app/models/api/OrgService/aa1005.interface';
import { OrganizationComponent } from 'src/app/shared/organization/organization.component';
import { BaseComponent } from '../../base-component';
import { TransformMenuNamePipe } from 'src/app/shared/pipes/transform-menu-name.pipe';

@Component({
  selector: 'app-ac1002',
  templateUrl: './ac1002.component.html',
  styleUrls: ['./ac1002.component.css'],
  providers: [MessageService, ConfirmationService, DynamicDialogRef]
})
export class Ac1002Component extends BaseComponent implements OnInit {

  @ViewChild('dialog') _dialog!: DialogComponent;
  @ViewChild('organization') _organization!: OrganizationComponent;

  formOperate = FormOperate;
  orgs?: AA1002List[];
  dialogTitle: string = '';
  selectedOrg?: AA1005Resp;

  constructor(
    route: ActivatedRoute,
    tr: TransformMenuNamePipe,
    private router: Router,
    private translate: TranslateService,
    private toolService: ToolService,
    private siderbar: SidebarService,
    private messageService: MessageService,
    private orgService: TOrgService,
    private dialogService: DialogService,
    private confirmationService: ConfirmationService
  ) {
    super(route, tr)
  }

  ngOnInit() {
    this.getOrgList();
  }

  create(show: boolean) {
    const codes = ['dialog.create', 'message.create', 'message.organization', 'message.success'];
    this.translate.get(codes).subscribe(dict => {
      this.dialogTitle = dict['dialog.create'];
      // let data: FormParams = {
      //     operate: FormOperate.create,
      //     displayInDialog: true,
      //     afterCloseCallback: (res) => {
      //         if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
      //             this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.organization']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
      //             window.setTimeout(() => {
      //                 this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
      //                     this.router.navigate(['ac10', 'ac1002']);
      //                 });
      //             }, 1000);
      //         }
      //     }
      // }
      // this._dialog.open(OrgFormComponent, data);
      const ref = this.dialogService.open(OrgFormComponent, {
        data: {
          operate: FormOperate.create,
          displayInDialog: true,
        },
        header: this.dialogTitle,
        width: '50vw',
        height: '100vh'
      });

      ref.onClose.subscribe(resOb => {
        if(resOb){
          resOb.subscribe(res => {
            if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
              this.messageService.add({ severity: 'success', summary: `${dict['message.create']} ${dict['message.organization']}`, detail: `${dict['message.create']} ${dict['message.success']}!` });
              window.setTimeout(() => {
                this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
                  this.router.navigate(['ac10', 'ac1002']);
                });
              }, 1000);
            }
          })
        }
      })
    });
  }

  getOrgList() {
    this.orgService.queryTOrgList({}).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.orgs = res.RespBody.orgList;
      }
    });
  }

  showDetail(org: AA1002List) {
    this._organization.filter();
    const codes = ['dialog.detail_query'];
    this.translate.get(codes).pipe(
      switchMap(dict => this.openDialog$(org, dict))
    ).subscribe();
    return false;
  }

  openDialog$(rowData: AA1002List, dict: any): Observable<boolean> {
    return Observable.create(obser => {
      // if (rowData.orgID == '100000' || rowData.parentID == '') { // default root 不可修改
      //   obser.next(true);
      //   return;
      // }
      this.dialogTitle = dict['dialog.detail_query'];
      //預埋api
      let ReqBody = {
        orgId: rowData.orgID
      } as AA1005Req;
      this.orgService.queryTOrgDetail(ReqBody).subscribe(res => {
        if (this.toolService.checkDpSuccess(res.ResHeader)) {
          // let detail = {};
          // detail = res.RespBody;
          // let data: FormParams = {
          //     data: detail,
          //     afterCloseCallback: (r) => {
          //         // console.log(r)
          //         if (r) {
          //             this.showDialog(r.data, r.operate);
          //         }
          //     }
          // }
          // this._dialog.open(OrgDetailComponent, data);
          const ref = this.dialogService.open(OrgDetailComponent, {
            data: res.RespBody,
            width: '50vw',
            height: '100vh'
          })

          ref.onClose.subscribe(res => {
            if (res) {
              this.showDialog(res.data, res.operate);
            }
          })

          // obser.next(true);
        }
      });
    });
  }

  showDialog(rowData: AA1005Resp, operation: FormOperate) {
    const codes = ['dialog.update', 'message.success', 'message.update', 'message.organization', 'cfm_del_org', 'dept_id', 'dept'];
    this.translate.get(codes).pipe(
      switchMap(dict => {
        return Observable.create(obser => {
          switch (operation) {
            case FormOperate.update:
              this.dialogTitle = dict['dialog.update'];
              // let data: FormParams = {
              //   operate: FormOperate.update,
              //   data: rowData,
              //   displayInDialog: true,
              //   afterCloseCallback: (res) => {
              //     // console.log(res)
              //     if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
              //       // this.siderbar.reset();
              //       this.messageService.add({
              //         severity: 'success', summary: `${dict['message.update']} ${dict['message.organization']}`, detail: `${dict['message.update']} ${dict['message.success']}!`
              //       });
              //       window.setTimeout(() => {
              //         this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
              //           this.router.navigate(['ac10', 'ac1002']);
              //         });
              //       }, 1000);
              //     }
              //   }
              // }
              // this._dialog.open(OrgFormComponent, data);

              const ref = this.dialogService.open(OrgFormComponent, {
                header: this.dialogTitle,
                data: {
                  operate: FormOperate.update,
                  data: rowData,
                  displayInDialog: true,
                },
                width: '50vw',
                height: '100vh'
              })

              ref.onClose.subscribe(resOb => {
                if(resOb) {resOb.subscribe(res => {
                  // console.log(res)
                  if (res && this.toolService.checkDpSuccess(res.ResHeader)) {
                    // this.siderbar.reset();
                    this.messageService.add({
                      severity: 'success', summary: `${dict['message.update']} ${dict['message.organization']}`, detail: `${dict['message.update']} ${dict['message.success']}!`
                    });
                    window.setTimeout(() => {
                      this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
                        this.router.navigate(['ac10', 'ac1002']);
                      });
                    }, 1000);
                  }
                })
                }

              })
              // obser.next(true);
              break;
            case FormOperate.delete:
              this.selectedOrg = rowData;
              // this.messageService.clear();
              // this.messageService.add({ key: 'delete', sticky: true, severity: 'error', summary: dict['cfm_del_org'], detail: `${dict['dept_id']} : ${rowData.orgId} , ${dict['dept']} : ${rowData.orgName}` });

              this.confirmationService.confirm({
                header: dict['cfm_del_org'],
                message: `${dict['dept_id']} : ${rowData.orgId} , ${dict['dept']} : ${rowData.orgName}`,
                accept: () => {
                    this.onConfirm();
                }
              });
              break;
          }
        });
      })
    ).subscribe();
    return false;
  }

  async onConfirm() {
    this.messageService.clear();
    const codes = ['message.delete', 'message.organization', 'message.success'];
    const dict = await this.toolService.getDict(codes);
    let ReqBody = {
      orgId: this.selectedOrg!.orgId,
      orgName: this.selectedOrg!.orgName
    } as AA1004Req;
    this.orgService.deleteTOrgByOrgId(ReqBody).subscribe(res => {
      if (this.toolService.checkDpSuccess(res.ResHeader)) {
        this.messageService.add({
          severity: 'success', summary: `${dict['message.delete']} ${dict['message.organization']}`, detail: `${dict['message.delete']} ${dict['message.success']}!`
        });
        window.setTimeout(() => {
          this.router.navigateByUrl('/RefreshComponent', { skipLocationChange: true }).then(() => {
            this.router.navigate(['ac10', 'ac1002']);
          });
        }, 500);
      }
    })
  }

  onReject() {
    this.messageService.clear();
  }
}
